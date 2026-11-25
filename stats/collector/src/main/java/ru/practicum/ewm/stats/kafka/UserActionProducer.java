package ru.practicum.ewm.stats.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.configuration.KafkaTopicProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Component
public class UserActionProducer implements AutoCloseable {

    private final KafkaProducer<Long, SpecificRecordBase> producer;
    private final String userActionTopic;

    public UserActionProducer(
            KafkaProducer<Long, SpecificRecordBase> producer,
            KafkaTopicProperties topicProperties) {
        this.producer = producer;
        this.userActionTopic = topicProperties.userActions();
    }

    public void send(SpecificRecordBase userAction, long key, Instant timestamp) {
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                userActionTopic,
                null,
                timestamp.toEpochMilli(),
                key,
                userAction
        );

        Future<RecordMetadata> futureMeta = producer.send(record);

        try {
            RecordMetadata meta = futureMeta.get();
            log.debug("Оправлено сообщение: topic={}, offset={}, userId={}",
                    meta.topic(), meta.offset(), key);
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Ошибка при отправлено сообщения в топик {}: {}", userActionTopic, e.getMessage());
            throw new KafkaException(e);
        }
    }

    @Override
    public void close() {
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }
}
