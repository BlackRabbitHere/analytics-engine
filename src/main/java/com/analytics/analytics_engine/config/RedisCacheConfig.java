package com.analytics.analytics_engine.config;

import com.analytics.analytics_engine.dto.UserActivitySummaryDTO;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

// Marks this class as a Spring configuration class
@Configuration(proxyBeanMethods = false)

// Enables Spring's caching functionality
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 1)

public class RedisCacheConfig {

    // Registers the customizer as a Spring Bean
    @Bean
    RedisCacheManagerBuilderCustomizer activitySummaryCacheCustomizer(
            JsonMapper jsonMapper) {

        // Converts DTO objects to JSON and back
        var serializer = new JacksonJsonRedisSerializer<>(
                jsonMapper,
                UserActivitySummaryDTO.class
        );

        // Defines how Redis should cache the data
        var cacheConfig = RedisCacheConfiguration.defaultCacheConfig()

                // Expire cached entries after 60 seconds
                .entryTtl(Duration.ofSeconds(60))

                // Do not allow null values in this cache
                .disableCachingNullValues()

                // Store cache values using JSON serialization
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer)
                );

        // Apply this configuration to activity-summary
        return builder -> builder.withCacheConfiguration(
                "activity-summary",
                cacheConfig
        );
    }
}