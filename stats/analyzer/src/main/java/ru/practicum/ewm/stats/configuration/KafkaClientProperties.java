package ru.practicum.ewm.stats.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaClientProperties(
        String bootstrapServers,
        Consumer consumer
) {
    public record Consumer(
            Boolean enableAutoCommit,
            String autoOffsetReset,
            ConsumerGroup userActions,
            ConsumerGroup eventSimilarity
    ) {
    }

    public record ConsumerGroup(
            String groupId,
            String keyDeserializer,
            String valueDeserializer
    ) {
    }
}
