package ru.practicum.ewm.stats.configuration;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaConfiguration {

    @Bean
    public KafkaConsumer<Long, UserActionAvro> kafkaConsumer(KafkaClientProperties kafkaProperties) {
        KafkaClientProperties.Consumer consumer = kafkaProperties.consumer();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumer.groupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumer.keyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumer.valueDeserializer());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumer.enableAutoCommit());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumer.autoOffsetReset());

        return new KafkaConsumer<>(props);
    }

    @Bean
    public KafkaProducer<String, SpecificRecordBase> kafkaProducer(KafkaClientProperties kafkaProperties) {
        KafkaClientProperties.Producer producer = kafkaProperties.producer();
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producer.keySerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producer.valueSerializer());

        return new KafkaProducer<>(props);
    }
}
