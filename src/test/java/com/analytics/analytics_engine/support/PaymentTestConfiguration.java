package com.analytics.analytics_engine.support;

import com.analytics.analytics_engine.model.Payment;
import com.analytics.analytics_engine.repository.OrderRepository;
import com.analytics.analytics_engine.repository.PaymentRepository;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Test-only hooks around real repository calls; all persistence and locks still use MySQL. */
@TestConfiguration(proxyBeanMethods = false)
public class PaymentTestConfiguration {

    @Bean
    static RepositoryProbe repositoryProbe() {
        return new RepositoryProbe();
    }

    public static final class RepositoryProbe implements BeanPostProcessor {
        private volatile Consumer<Long> beforeOrderLock = id -> {};
        private volatile BiConsumer<String, Optional<Payment>> afterReferenceLookup = (ref, result) -> {};
        private volatile Consumer<Payment> afterPaymentInsert = payment -> {};

        public void beforeOrderLock(Consumer<Long> action) {
            beforeOrderLock = action;
        }

        public void afterReferenceLookup(BiConsumer<String, Optional<Payment>> action) {
            afterReferenceLookup = action;
        }

        public void afterPaymentInsert(Consumer<Payment> action) {
            afterPaymentInsert = action;
        }

        public void reset() {
            beforeOrderLock = id -> {};
            afterReferenceLookup = (ref, result) -> {};
            afterPaymentInsert = payment -> {};
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (!(bean instanceof OrderRepository) && !(bean instanceof PaymentRepository)) {
                return bean;
            }
            ProxyFactory proxy = new ProxyFactory(bean);
            proxy.addAdvice((MethodInterceptor) invocation -> {
                String method = invocation.getMethod().getName();
                if (bean instanceof OrderRepository && "findByIdForUpdate".equals(method)) {
                    beforeOrderLock.accept((Long) invocation.getArguments()[0]);
                }

                Object result = invocation.proceed();

                if (bean instanceof PaymentRepository) {
                    if ("findByProviderReference".equals(method)) {
                        @SuppressWarnings("unchecked")
                        Optional<Payment> payment = (Optional<Payment>) result;
                        afterReferenceLookup.accept((String) invocation.getArguments()[0], payment);
                    } else if ("saveAndFlush".equals(method)) {
                        afterPaymentInsert.accept((Payment) result);
                    }
                }
                return result;
            });
            return proxy.getProxy();
        }
    }
}
