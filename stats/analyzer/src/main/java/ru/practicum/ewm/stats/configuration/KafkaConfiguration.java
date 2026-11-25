package ru.practicum.ewm.stats.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaClientProperties kafkaProperties;

    @Bean
    public KafkaConsumer<Long, UserActionAvro> userActionConsumer() {
        return new KafkaConsumer<>(consumerProperties(kafkaProperties.consumer().userActions()));
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> eventSimilarityConsumer() {
        return new KafkaConsumer<>(consumerProperties(kafkaProperties.consumer().eventSimilarity()));
    }

    private Properties consumerProperties(KafkaClientProperties.ConsumerGroup group) {
        KafkaClientProperties.Consumer consumer = kafkaProperties.consumer();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group.groupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, group.keyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, group.valueDeserializer());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumer.enableAutoCommit());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumer.autoOffsetReset());
        return props;
    }
}
