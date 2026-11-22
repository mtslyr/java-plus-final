package ru.practicum.ewm.stats.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private Boolean enableAutoCommit;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Bean
    public KafkaConsumer<Long, UserActionAvro> userActionConsumer(
            @Value("${spring.kafka.consumer.user-actions.group-id}") String groupId,
            @Value("${spring.kafka.consumer.user-actions.key-deserializer}") String keyDeserializer,
            @Value("${spring.kafka.consumer.user-actions.value-deserializer}") String valueDeserializer) {
        return new KafkaConsumer<>(consumerProperties(groupId, keyDeserializer, valueDeserializer));
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> eventSimilarityConsumer(
            @Value("${spring.kafka.consumer.event-similarity.group-id}") String groupId,
            @Value("${spring.kafka.consumer.event-similarity.key-deserializer}") String keyDeserializer,
            @Value("${spring.kafka.consumer.event-similarity.value-deserializer}") String valueDeserializer) {
        return new KafkaConsumer<>(consumerProperties(groupId, keyDeserializer, valueDeserializer));
    }

    private Properties consumerProperties(String groupId, String keyDeserializer, String valueDeserializer) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return props;
    }
}
