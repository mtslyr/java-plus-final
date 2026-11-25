package ru.practicum.ewm.stats.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka.topic")
public record KafkaTopicProperties(
        String userActions,
        String eventSimilarity
) {
}
