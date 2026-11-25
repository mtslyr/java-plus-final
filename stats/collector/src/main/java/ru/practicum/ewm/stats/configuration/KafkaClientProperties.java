package ru.practicum.ewm.stats.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaClientProperties(
        String bootstrapServers,
        Producer producer
) {
    public record Producer(
            String keySerializer,
            String valueSerializer
    ) {
    }
}
