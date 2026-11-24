package ru.practicum.ewm.stats.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaClientProperties(
        String bootstrapServers,
        Consumer consumer,
        Producer producer
) {
    public record Consumer(
            String groupId,
            Boolean enableAutoCommit,
            String autoOffsetReset,
            String keyDeserializer,
            String valueDeserializer
    ) {
    }

    public record Producer(
            String keySerializer,
            String valueSerializer
    ) {
    }
}
