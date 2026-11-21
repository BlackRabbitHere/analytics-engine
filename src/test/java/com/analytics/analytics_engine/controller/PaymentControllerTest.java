package com.analytics.analytics_engine.controller;

import com.analytics.analytics_engine.dto.PaymentRequestDTO;
import com.analytics.analytics_engine.dto.PaymentResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.service.PaymentService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Fast HTTP/validation/error-contract tests. The separate MySQL suite proves persistence. */
@WebMvcTest(controllers = PaymentController.class, properties =
        "logging.level.com.analytics.analytics_engine.exception.MyGlobalExceptionHandler=OFF")
class PaymentControllerTest {

    private static final String URL = "/api/payments/callback";
    private static final String VALID_REQUEST = body("1", "\"pay-1\"", "100.00");

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PaymentService paymentService;

    @Test
    void returnsPaymentAsJsonAndPassesValidatedValuesToService() throws Exception {
        when(paymentService.completePayment(any())).thenReturn(response());

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paymentId").value(10))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.providerReference").value("pay-1"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.orderStatus").value("PAID"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.refundedAt").doesNotExist());

        ArgumentCaptor<PaymentRequestDTO> request = ArgumentCaptor.forClass(PaymentRequestDTO.class);
        verify(paymentService).completePayment(request.capture());
        assertThat(request.getValue().getOrderId()).isEqualTo(1L);
        assertThat(request.getValue().getProviderReference()).isEqualTo("pay-1");
        assertThat(request.getValue().getPaymentAmount()).isEqualByComparingTo("100.00");
    }

    @ParameterizedTest(name = "invalid field: {1}, body: {0}")
    @MethodSource("invalidFields")
    void rejectsInvalidFieldsBeforeCallingService(String request, String field) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.httpStatus").value(400))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors." + field).isNotEmpty());

        verifyNoInteractions(paymentService);
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("{}", "orderId"),
                Arguments.of("{\"providerReference\":\"pay-1\",\"paymentAmount\":100}", "orderId"),
                Arguments.of(body("null", "\"pay-1\"", "100"), "orderId"),
                Arguments.of(body("0", "\"pay-1\"", "100"), "orderId"),
                Arguments.of(body("-1", "\"pay-1\"", "100"), "orderId"),
                Arguments.of("{\"orderId\":1,\"paymentAmount\":100}", "providerReference"),
                Arguments.of(body("1", "null", "100"), "providerReference"),
                Arguments.of(body("1", "\"\"", "100"), "providerReference"),
                Arguments.of(body("1", "\"   \"", "100"), "providerReference"),
                Arguments.of(body("1", "\"\\t\\n\"", "100"), "providerReference"),
                Arguments.of(body("1", "\"" + "r".repeat(101) + "\"", "100"), "providerReference"),
                Arguments.of("{\"orderId\":1,\"providerReference\":\"pay-1\"}", "paymentAmount"),
                Arguments.of(body("1", "\"pay-1\"", "null"), "paymentAmount"),
                Arguments.of(body("1", "\"pay-1\"", "0"), "paymentAmount"),
                Arguments.of(body("1", "\"pay-1\"", "-0.01"), "paymentAmount"),
                Arguments.of(body("1", "\"pay-1\"", "0.001"), "paymentAmount"),
                Arguments.of(body("1", "\"pay-1\"", "100.001"), "paymentAmount"),
                Arguments.of(body("1", "\"pay-1\"", "10000000000.00"), "paymentAmount"),
                Arguments.of(body("1", "\"pay-1\"", "1e10"), "paymentAmount")
        );
    }

    @ParameterizedTest
    @MethodSource("unreadableBodies")
    void rejectsMissingMalformedOrUnconvertibleJson(String request) throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_BODY"));

        verifyNoInteractions(paymentService);
    }

    static Stream<String> unreadableBodies() {
        return Stream.of(
                "", "null", "{", "[]", "\"text\"",
                body("9223372036854775808", "\"pay-1\"", "100"),
                body("1.5", "\"pay-1\"", "100"),
                body("true", "\"pay-1\"", "100"),
                body("{}", "\"pay-1\"", "100"),
                body("1", "{}", "100"),
                body("1", "\"pay-1\"", "\"not-money\""),
                body("1", "\"pay-1\"", "true"),
                body("1", "\"pay-1\"", "{}")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "100.00", "9999999999.99"})
    void acceptsValidAmountBoundaries(String amount) throws Exception {
        when(paymentService.completePayment(any())).thenReturn(response());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "\"pay-1\"", amount)))
                .andExpect(status().isOk());
    }

    @Test
    void acceptsExactlyOneHundredReferenceCharacters() throws Exception {
        when(paymentService.completePayment(any())).thenReturn(response());
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(body("1", "\"" + "r".repeat(100) + "\"", "100")))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("businessErrors")
    void preservesBusinessErrorStatusAndCode(ErrorCode error) throws Exception {
        when(paymentService.completePayment(any())).thenThrow(new APIException(error));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().is(error.getHttpStatus().value()))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.httpStatus").value(error.getHttpStatus().value()))
                .andExpect(jsonPath("$.errorCode").value(error.name()));
    }

    static Stream<ErrorCode> businessErrors() {
        return Stream.of(ErrorCode.ORDER_NOT_FOUND, ErrorCode.PAYMENT_CONFLICT,
                ErrorCode.PAYMENT_AMOUNT_MISMATCH, ErrorCode.ORDER_NOT_PAYABLE,
                ErrorCode.VALIDATION_FAILED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"uc_payments_order", "payments.uc_payments_order",
            "uc_payments_provider_reference", "payments.uc_payments_provider_reference"})
    void translatesOnlyKnownPaymentConstraintFailuresToConflict(String constraint) throws Exception {
        when(paymentService.completePayment(any())).thenThrow(integrityFailure(constraint));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_CONFLICT"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "FK_PAYMENTS_ON_ORDER")
    void unrelatedOrUnnamedDatabaseFailuresRemainServerErrors(String constraint) throws Exception {
        when(paymentService.completePayment(any())).thenThrow(integrityFailure(constraint));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("private database details"))));
    }

    @Test
    void unexpectedFailuresDoNotExposeInternalExceptionMessages() throws Exception {
        when(paymentService.completePayment(any()))
                .thenThrow(new IllegalStateException("private database details"));
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("private database details"))));
    }

    @Test
    void rejectsUnsupportedContentType() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content(VALID_REQUEST))
                .andExpect(status().isUnsupportedMediaType());
        verifyNoInteractions(paymentService);
    }

    @Test
    void rejectsWrongHttpMethod() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(paymentService);
    }

    private static String body(String orderId, String reference, String amount) {
        return "{\"orderId\":" + orderId + ",\"providerReference\":" + reference
                + ",\"paymentAmount\":" + amount + "}";
    }

    private static PaymentResponseDTO response() {
        return new PaymentResponseDTO(10L, 1L, "pay-1", new BigDecimal("100.00"),
                "SUCCESS", Instant.parse("2026-10-25T00:00:00Z"), null, "PAID");
    }

    private static DataIntegrityViolationException integrityFailure(String constraint) {
        return new DataIntegrityViolationException("private database details",
                new ConstraintViolationException("duplicate", new SQLException("duplicate", "23000", 1062),
                        constraint));
    }
}
