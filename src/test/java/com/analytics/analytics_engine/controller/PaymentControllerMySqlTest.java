package com.analytics.analytics_engine.controller;

import com.analytics.analytics_engine.model.Lead;
import com.analytics.analytics_engine.model.Order;
import com.analytics.analytics_engine.model.Payment;
import com.analytics.analytics_engine.model.User;
import com.analytics.analytics_engine.repository.LeadRepository;
import com.analytics.analytics_engine.repository.OrderRepository;
import com.analytics.analytics_engine.repository.PaymentRepository;
import com.analytics.analytics_engine.repository.UserRepository;
import com.analytics.analytics_engine.support.MySqlTestConfiguration;
import com.analytics.analytics_engine.support.PaymentTestConfiguration;
import com.analytics.analytics_engine.support.PaymentTestConfiguration.RepositoryProbe;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.testcontainers.mysql.MySQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * HTTP -> real service -> MySQL, including actual commits and independent worker transactions.
 * Deliberately no test-level @Transactional: it could hide a missing service commit or rollback.
 */
@SpringBootTest(properties = {
        "logging.level.com.analytics.analytics_engine.exception.MyGlobalExceptionHandler=OFF",
        "logging.level.org.hibernate.orm.jdbc.error=OFF"
})
@AutoConfigureMockMvc
@Import({MySqlTestConfiguration.class, PaymentTestConfiguration.class})
@Execution(ExecutionMode.SAME_THREAD)
class PaymentControllerMySqlTest {

    private static final String URL = "/api/payments/callback";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate transactions;
    @Autowired UserRepository users;
    @Autowired LeadRepository leads;
    @Autowired OrderRepository orders;
    @Autowired PaymentRepository payments;
    @Autowired RepositoryProbe probe;
    @Autowired MySQLContainer mysql;

    @BeforeEach
    void requireDisposableDatabase() {
        assertThat(jdbc.queryForObject("select database()", String.class))
                .as("Integration tests must use the disposable Testcontainers database")
                .isEqualTo("analytics_payment_tests");
    }

    @AfterEach
    void resetRepositoryHooks() {
        probe.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "100.00", "9999999999.99"})
    void commitsPaymentOrderAndLeadAtValidAmountBoundaries(String amount) throws Exception {
        Fixture fixture = fixture(amount);
        String reference = reference();

        JsonNode response = successful(callback(fixture, reference, amount));
        Snapshot saved = snapshot(fixture);

        assertThat(response.path("orderId").asLong()).isEqualTo(fixture.orderId());
        assertThat(response.path("providerReference").asString()).isEqualTo(reference);
        assertThat(new BigDecimal(response.path("amount").asString())).isEqualByComparingTo(amount);
        assertThat(response.path("status").asString()).isEqualTo("SUCCESS");
        assertThat(response.path("orderStatus").asString()).isEqualTo("PAID");
        assertPaid(saved);
        Map<String, Object> payment = saved.payments().getFirst();
        assertThat(((Number) payment.get("id")).longValue())
                .isEqualTo(response.path("paymentId").asLong());
        assertThat(payment.get("provider_reference")).isEqualTo(reference);
        assertThat((BigDecimal) payment.get("amount")).isEqualByComparingTo(amount);
        assertThat(saved.paidAt()).isEqualTo(payment.get("created_at"));
    }

    @Test
    void referenceIsPreservedIncludingCaseHyphensAndSurroundingUnderscores() throws Exception {
        Fixture fixture = fixture("100");
        String reference = "_Pay-Case_" + UUID.randomUUID() + "_";
        successful(callback(fixture, reference, "100"));
        assertThat(snapshot(fixture).payments().getFirst().get("provider_reference"))
                .isEqualTo(reference);
    }

    @Test
    void acceptsOneHundredCharacterReference() throws Exception {
        Fixture fixture = fixture("100");
        String reference = UUID.randomUUID() + "r".repeat(64);
        assertThat(reference).hasSize(100);
        successful(callback(fixture, reference, "100"));
        assertThat(snapshot(fixture).payments().getFirst().get("provider_reference"))
                .isEqualTo(reference);
    }

    @Test
    void repeatedCallbackReturnsSamePaymentWithoutChangingAnyStoredValues() throws Exception {
        Fixture fixture = fixture("100");
        String reference = reference();
        long paymentId = successful(callback(fixture, reference, "100.00"))
                .path("paymentId").asLong();
        Snapshot before = snapshot(fixture);

        for (String equivalentAmount : List.of("100", "100.0", "100.00")) {
            JsonNode replay = successful(callback(fixture, reference, equivalentAmount));
            assertThat(replay.path("paymentId").asLong()).isEqualTo(paymentId);
            assertThat(snapshot(fixture)).isEqualTo(before);
        }
    }

    @Test
    void changedAmountOnExistingReferenceIsConflictAndPreservesData() throws Exception {
        Fixture fixture = fixture("100");
        String reference = reference();
        successful(callback(fixture, reference, "100"));
        Snapshot before = snapshot(fixture);

        error(callback(fixture, reference, "100.01"), 409, "PAYMENT_CONFLICT");
        assertThat(snapshot(fixture)).isEqualTo(before);
    }

    @Test
    void existingReferenceCannotBeUsedForAnotherOrder() throws Exception {
        Fixture first = fixture("100");
        Fixture second = fixture("100");
        String reference = reference();
        successful(callback(first, reference, "100"));
        Snapshot beforeFirst = snapshot(first);
        Snapshot beforeSecond = snapshot(second);

        error(callback(second, reference, "100"), 409, "PAYMENT_CONFLICT");
        assertThat(snapshot(first)).isEqualTo(beforeFirst);
        assertThat(snapshot(second)).isEqualTo(beforeSecond);
        assertPending(snapshot(second));
    }

    @Test
    void paidOrderCannotReceiveAnotherReference() throws Exception {
        Fixture fixture = fixture("100");
        successful(callback(fixture, reference(), "100"));
        Snapshot before = snapshot(fixture);

        error(callback(fixture, reference(), "100"), 409, "PAYMENT_CONFLICT");
        assertThat(snapshot(fixture)).isEqualTo(before);
    }

    @Test
    void changingOnlyReferenceCaseIsNotAnIdenticalReplay() throws Exception {
        Fixture fixture = fixture("100");
        String reference = "Pay-" + UUID.randomUUID();
        successful(callback(fixture, reference, "100"));
        Snapshot before = snapshot(fixture);

        error(callback(fixture, reference.toLowerCase(java.util.Locale.ROOT), "100"),
                409, "PAYMENT_CONFLICT");
        assertThat(snapshot(fixture)).isEqualTo(before);
    }

    @Test
    void documentsCaseInsensitiveDatabaseReferenceUniqueness() throws Exception {
        Fixture first = fixture("100");
        Fixture second = fixture("100");
        String reference = "Case-Sensitive-" + UUID.randomUUID();
        successful(callback(first, reference, "100"));
        Snapshot firstBefore = snapshot(first);
        Snapshot secondBefore = snapshot(second);

        // Current limitation: the migration inherits MySQL's case-insensitive default collation.
        // If provider references are case-sensitive, change the migration and this expectation.
        error(callback(second, reference.toLowerCase(java.util.Locale.ROOT), "100"),
                409, "PAYMENT_CONFLICT");
        assertThat(jdbc.queryForObject("""
                select COLLATION_NAME from information_schema.COLUMNS
                where TABLE_SCHEMA = database() and TABLE_NAME = 'payments'
                  and COLUMN_NAME = 'provider_reference'
                """, String.class)).endsWith("_ci");
        assertThat(snapshot(first)).isEqualTo(firstBefore);
        assertThat(snapshot(second)).isEqualTo(secondBefore);
    }

    @Test
    void freshMismatchedAmountIsBadRequestAndOrderRemainsPending() throws Exception {
        Fixture fixture = fixture("100");
        Snapshot before = snapshot(fixture);

        error(callback(fixture, reference(), "99.99"), 400, "PAYMENT_AMOUNT_MISMATCH");
        assertThat(snapshot(fixture)).isEqualTo(before);
        assertPending(snapshot(fixture));
    }

    @Test
    void missingOrderIsNotFound() throws Exception {
        Fixture nonexistent = new Fixture(Long.MAX_VALUE, Long.MAX_VALUE);
        error(callback(nonexistent, reference(), "100"), 404, "ORDER_NOT_FOUND");
        assertThat(jdbc.queryForObject("select count(*) from payments where order_id = ?",
                Long.class, nonexistent.orderId())).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "0", "-1", "0.001", "10000000000.00"})
    void invalidAmountsNeverWriteToDatabase(String amount) throws Exception {
        Fixture fixture = fixture("100");
        Snapshot before = snapshot(fixture);
        MvcResult result = mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"orderId\":" + fixture.orderId()
                        + ",\"providerReference\":\"" + reference()
                        + "\",\"paymentAmount\":" + amount + "}")).andReturn();

        error(result, 400, "VALIDATION_FAILED");
        assertThat(snapshot(fixture)).isEqualTo(before);
    }

    @Test
    void invalidReplayAmountIsRejectedBeforePaymentLookup() throws Exception {
        Fixture fixture = fixture("100");
        String reference = reference();
        successful(callback(fixture, reference, "100"));
        Snapshot before = snapshot(fixture);

        error(callback(fixture, reference, "0"), 400, "VALIDATION_FAILED");
        assertThat(snapshot(fixture)).isEqualTo(before);
    }

    @Test
    void clientCannotChoosePaymentOrOrderStatus() throws Exception {
        Fixture fixture = fixture("100");
        String request = json.writeValueAsString(Map.of(
                "orderId", fixture.orderId(), "providerReference", reference(),
                "paymentAmount", 100, "status", "REFUNDED", "orderStatus", "REFUNDED"));

        successful(mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(request)).andReturn());
        assertPaid(snapshot(fixture));
    }

    @Test
    void originalCallbackAfterRefundPreservesBothRefundsAndOriginalPaymentTime() throws Exception {
        Fixture fixture = fixture("100");
        String reference = reference();
        long paymentId = successful(callback(fixture, reference, "100"))
                .path("paymentId").asLong();
        refundFixture(fixture);
        Snapshot before = snapshot(fixture);

        JsonNode replay = successful(callback(fixture, reference, "100.00"));
        assertThat(replay.path("paymentId").asLong()).isEqualTo(paymentId);
        assertThat(replay.path("status").asString()).isEqualTo("REFUNDED");
        assertThat(replay.path("orderStatus").asString()).isEqualTo("REFUNDED");
        assertThat(replay.path("refundedAt").isNull()).isFalse();
        assertThat(snapshot(fixture)).isEqualTo(before);
    }

    @Test
    void refundedOrderRejectsNewReferenceAndChangedOriginalAmount() throws Exception {
        Fixture fixture = fixture("100");
        String reference = reference();
        successful(callback(fixture, reference, "100"));
        refundFixture(fixture);
        Snapshot before = snapshot(fixture);

        error(callback(fixture, reference(), "100"), 409, "PAYMENT_CONFLICT");
        error(callback(fixture, reference, "101"), 409, "PAYMENT_CONFLICT");
        assertThat(snapshot(fixture)).isEqualTo(before);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PAID", "REFUNDED"})
    void nonPendingOrderWithoutPaymentIsNotAllowedToCreateOne(String status) throws Exception {
        Fixture fixture = fixture("100");
        transactions.executeWithoutResult(transaction -> {
            Order order = orders.findByIdForUpdate(fixture.orderId()).orElseThrow();
            Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
            order.markPaid(now);
            if ("REFUNDED".equals(status)) {
                order.refund(now);
            }
        });
        Snapshot before = snapshot(fixture);

        error(callback(fixture, reference(), "100"), 409, "ORDER_NOT_PAYABLE");
        assertThat(snapshot(fixture)).isEqualTo(before);
    }

    @Test
    void failureAfterRealPaymentInsertRollsBackThatInsertAndAllowsNormalRetry() throws Exception {
        Fixture fixture = fixture("100");
        String reference = reference();
        Snapshot before = snapshot(fixture);
        AtomicBoolean inserted = new AtomicBoolean();

        probe.afterPaymentInsert(saved -> {
            assertThat(jdbc.queryForObject("select count(*) from payments where order_id = ?",
                    Long.class, fixture.orderId())).isEqualTo(1L);
            inserted.set(true);
            throw new IllegalStateException("test-only failure after flushed INSERT");
        });

        error(callback(fixture, reference, "100"), 500, "INTERNAL_ERROR");
        assertThat(inserted.get()).isTrue();
        assertThat(snapshot(fixture)).isEqualTo(before);

        probe.reset();
        successful(callback(fixture, reference, "100"));
        assertPaid(snapshot(fixture));
    }

    @Test
    void databaseFailureAtCommitRollsBackPaymentOrderAndLeadTogether() throws Exception {
        Fixture fixture = fixture("100");
        Snapshot before = snapshot(fixture);
        String trigger = "payment_test_" + UUID.randomUUID().toString().replace("-", "");

        // A test-only trigger forces a real database failure when the dirty lead is flushed.
        jdbc.execute("CREATE TRIGGER " + trigger + " BEFORE UPDATE ON leads FOR EACH ROW "
                + "BEGIN IF NEW.id = " + fixture.leadId() + " THEN "
                + "SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'test-only commit failure'; "
                + "END IF; END");
        try {
            error(callback(fixture, reference(), "100"), 500, "INTERNAL_ERROR");
            assertThat(snapshot(fixture)).isEqualTo(before);
            assertPending(snapshot(fixture));
        } finally {
            jdbc.execute("DROP TRIGGER " + trigger);
        }
    }

    @Test
    @Timeout(40)
    void anotherTransactionHoldingOrderLockMakesCallbackWaitUntilRelease() throws Exception {
        Fixture fixture = fixture("100");
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch attempted = new CountDownLatch(1);
        try {
            Future<?> holder = workers.submit(() -> transactions.executeWithoutResult(transaction -> {
                orders.findByIdForUpdate(fixture.orderId()).orElseThrow();
                held.countDown();
                await(release);
            }));
            await(held);

            probe.beforeOrderLock(id -> {
                if (id.equals(fixture.orderId())) {
                    attempted.countDown();
                }
            });

            Future<MvcResult> waiting = workers.submit(() -> callback(fixture, reference(), "100"));
            await(attempted);
            assertThatThrownBy(() -> waiting.get(250, TimeUnit.MILLISECONDS))
                    .isInstanceOf(TimeoutException.class);
            assertPending(snapshot(fixture));

            release.countDown();
            holder.get(15, TimeUnit.SECONDS);
            successful(waiting.get(15, TimeUnit.SECONDS));
            assertPaid(snapshot(fixture));
        } finally {
            release.countDown();
            workers.shutdownNow();
            assertThat(workers.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    @Timeout(50)
    void documentsLockTimeoutAsGenericServerErrorAndConfirmsSafeRetry() throws Exception {
        Fixture fixture = fixture("100");
        Snapshot before = snapshot(fixture);
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        String reference = reference();
        try {
            Future<?> holder = workers.submit(() -> transactions.executeWithoutResult(transaction -> {
                orders.findByIdForUpdate(fixture.orderId()).orElseThrow();
                held.countDown();
                await(release, 30);
            }));
            await(held);
            Future<MvcResult> waiting = workers.submit(() -> callback(fixture, reference, "100"));

            // Current limitation: lock failures have no dedicated retryable error response.
            error(waiting.get(20, TimeUnit.SECONDS), 500, "INTERNAL_ERROR");
            assertThat(snapshot(fixture)).isEqualTo(before);

            release.countDown();
            holder.get(10, TimeUnit.SECONDS);
            successful(callback(fixture, reference, "100"));
            assertPaid(snapshot(fixture));
        } finally {
            release.countDown();
            workers.shutdownNow();
            assertThat(workers.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void documentsV2MigrationFailureWhenV1AlreadyContainsUsers() {
        // Use root only on the disposable container to create an independent upgrade fixture.
        JdbcTemplate root = new JdbcTemplate(new DriverManagerDataSource(
                mysql.getJdbcUrl(), "root", mysql.getPassword()));
        String schema = "payment_upgrade_" + UUID.randomUUID().toString().replace("-", "");
        try {
            Flyway.configure().dataSource(mysql.getJdbcUrl(), "root", mysql.getPassword())
                    .schemas(schema).defaultSchema(schema).target("1").load().migrate();
            root.update("INSERT INTO " + schema + ".users (created_at) VALUES (UTC_TIMESTAMP(6))");

            Flyway upgrade = Flyway.configure()
                    .dataSource(mysql.getJdbcUrl(), "root", mysql.getPassword())
                    .schemas(schema).defaultSchema(schema).load();

            // Current bug: V2 makes username NOT NULL before backfilling the existing rows.
            assertThatThrownBy(upgrade::migrate).isInstanceOf(FlywayException.class)
                    .hasMessageContaining("V2__add_username_to_users.sql");
        } finally {
            root.execute("DROP DATABASE IF EXISTS " + schema);
        }
    }

    @RepeatedTest(3)
    @Timeout(50)
    void simultaneousIdenticalCallbacksBothSucceedWithOnePayment() throws Exception {
        Fixture fixture = fixture("100");
        String reference = reference();
        List<MvcResult> results = together(
                () -> callback(fixture, reference, "100"),
                () -> callback(fixture, reference, "100.00"));

        long firstId = successful(results.get(0)).path("paymentId").asLong();
        long secondId = successful(results.get(1)).path("paymentId").asLong();
        assertThat(secondId).isEqualTo(firstId);
        assertPaid(snapshot(fixture));
    }

    @Test
    @Timeout(50)
    void simultaneousDifferentReferencesForOneOrderProduceOneSuccessAndOneConflict() throws Exception {
        Fixture fixture = fixture("100");
        List<MvcResult> results = together(
                () -> callback(fixture, reference(), "100"),
                () -> callback(fixture, reference(), "100"));

        assertThat(results).extracting(result -> result.getResponse().getStatus())
                .containsExactlyInAnyOrder(200, 409);
        for (MvcResult result : results) {
            if (result.getResponse().getStatus() == 409) {
                error(result, 409, "PAYMENT_CONFLICT");
            }
        }
        assertPaid(snapshot(fixture));
    }

    @RepeatedTest(3)
    @Timeout(50)
    void simultaneousDifferentOrdersSharingReferenceExerciseActualUniqueConstraint() throws Exception {
        Fixture first = fixture("100");
        Fixture second = fixture("100");
        String reference = reference();
        CountDownLatch bothReadMissingReference = new CountDownLatch(2);

        // Synchronize AFTER real reads, forcing both requests past the precheck before INSERT.
        probe.afterReferenceLookup((lookedUpReference, existing) -> {
            if (reference.equals(lookedUpReference)) {
                assertThat(existing).isEmpty();
                bothReadMissingReference.countDown();
                await(bothReadMissingReference);
            }
        });

        List<MvcResult> results = together(
                () -> callback(first, reference, "100"),
                () -> callback(second, reference, "100"));

        assertThat(results).extracting(result -> result.getResponse().getStatus())
                .containsExactlyInAnyOrder(200, 409);
        for (int index = 0; index < results.size(); index++) {
            Fixture fixture = index == 0 ? first : second;
            MvcResult result = results.get(index);
            if (result.getResponse().getStatus() == 200) {
                successful(result);
                assertPaid(snapshot(fixture));
            } else {
                error(result, 409, "PAYMENT_CONFLICT");
                assertPending(snapshot(fixture));
            }
        }
        assertThat(jdbc.queryForObject("select count(*) from payments where provider_reference = ?",
                Long.class, reference)).isEqualTo(1L);
    }

    private Fixture fixture(String amount) {
        return transactions.execute(transaction -> {
            User user = users.saveAndFlush(new User("payment-test-" + UUID.randomUUID()));
            Lead lead = leads.saveAndFlush(new Lead(user, "payment-test"));
            Order order = orders.saveAndFlush(new Order(user, lead, new BigDecimal(amount)));
            return new Fixture(order.getId(), lead.getId());
        });
    }

    private void refundFixture(Fixture fixture) {
        // There is no refund endpoint yet. Arrange an already-refunded committed state directly.
        transactions.executeWithoutResult(transaction -> {
            Order order = orders.findByIdForUpdate(fixture.orderId()).orElseThrow();
            Payment payment = payments.findByOrder_Id(fixture.orderId()).orElseThrow();
            Instant refundTime = Instant.now().truncatedTo(ChronoUnit.MICROS);
            order.refund(refundTime);
            payment.refund(refundTime);
        });
    }

    private MvcResult callback(Fixture fixture, String reference, String amount) throws Exception {
        return mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("orderId", fixture.orderId(),
                        "providerReference", reference, "paymentAmount", new BigDecimal(amount)))))
                .andReturn();
    }

    private JsonNode successful(MvcResult result) throws Exception {
        assertThat(result.getResponse().getStatus())
                .as("HTTP response: %s", result.getResponse().getContentAsString())
                .isEqualTo(200);
        return json.readTree(result.getResponse().getContentAsString());
    }

    private void error(MvcResult result, int status, String code) throws Exception {
        assertThat(result.getResponse().getStatus())
                .as("HTTP response: %s", result.getResponse().getContentAsString())
                .isEqualTo(status);
        JsonNode body = json.readTree(result.getResponse().getContentAsString());
        assertThat(body.path("success").asBoolean()).isFalse();
        assertThat(body.path("httpStatus").asInt()).isEqualTo(status);
        assertThat(body.path("errorCode").asString()).isEqualTo(code);
    }

    private Snapshot snapshot(Fixture fixture) {
        Map<String, Object> state = jdbc.queryForMap("""
                select o.status as order_status, o.paid_at, o.refunded_at,
                       l.status as lead_status, l.updated_at as lead_updated_at
                from orders o join leads l on l.id = o.lead_id where o.id = ?
                """, fixture.orderId());
        List<Map<String, Object>> storedPayments = jdbc.queryForList("""
                select id, provider_reference, amount, status, created_at, refunded_at
                from payments where order_id = ? order by id
                """, fixture.orderId());
        return new Snapshot((String) state.get("order_status"), state.get("paid_at"),
                state.get("refunded_at"), (String) state.get("lead_status"),
                state.get("lead_updated_at"), storedPayments);
    }

    private void assertPaid(Snapshot state) {
        assertThat(state.orderStatus()).isEqualTo("PAID");
        assertThat(state.leadStatus()).isEqualTo("CONVERTED");
        assertThat(state.paidAt()).isNotNull();
        assertThat(state.refundedAt()).isNull();
        assertThat(state.payments()).hasSize(1);
        assertThat(state.payments().getFirst().get("status")).isEqualTo("SUCCESS");
        assertThat(state.payments().getFirst().get("refunded_at")).isNull();
    }

    private void assertPending(Snapshot state) {
        assertThat(state.orderStatus()).isEqualTo("PENDING");
        assertThat(state.leadStatus()).isEqualTo("NEW");
        assertThat(state.paidAt()).isNull();
        assertThat(state.refundedAt()).isNull();
        assertThat(state.payments()).isEmpty();
    }

    private static String reference() {
        return "payment-test-" + UUID.randomUUID();
    }

    private static void await(CountDownLatch latch) {
        await(latch, 10);
    }

    private static void await(CountDownLatch latch, long seconds) {
        try {
            if (!latch.await(seconds, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out coordinating concurrent test requests");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Concurrent test interrupted", ex);
        }
    }

    private List<MvcResult> together(Callable<MvcResult> first, Callable<MvcResult> second)
            throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<MvcResult> firstResult = workers.submit(() -> {
                ready.countDown();
                await(start);
                return first.call();
            });
            Future<MvcResult> secondResult = workers.submit(() -> {
                ready.countDown();
                await(start);
                return second.call();
            });
            await(ready);
            start.countDown();
            return List.of(firstResult.get(20, TimeUnit.SECONDS), secondResult.get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            workers.shutdownNow();
            assertThat(workers.awaitTermination(15, TimeUnit.SECONDS)).isTrue();
        }
    }

    private record Fixture(Long orderId, Long leadId) {}

    private record Snapshot(String orderStatus, Object paidAt, Object refundedAt,
                            String leadStatus, Object leadUpdatedAt,
                            List<Map<String, Object>> payments) {}
}
