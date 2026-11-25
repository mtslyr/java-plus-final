package ru.practicum.ewm.stats.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.configuration.KafkaTopicProperties;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class EventSimilarityProducer implements AutoCloseable {

    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final String eventSimilarityTopic;

    private final AtomicReference<Exception> sendError = new AtomicReference<>();

    public EventSimilarityProducer(KafkaProducer<String, SpecificRecordBase> producer,
                                   KafkaTopicProperties topics) {
        this.producer = producer;
        this.eventSimilarityTopic = topics.eventSimilarity();
    }

    public void send(EventSimilarityAvro similarity) {
        String key = similarity.getEventA() + ":" + similarity.getEventB();
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                eventSimilarityTopic,
                null,
                similarity.getTimestamp().toEpochMilli(),
                key,
                similarity
        );

        producer.send(record, (meta, e) -> {
            if (e != null) {
                sendError.compareAndSet(null, e);
            } else {
                log.debug("Отправлено сходство: topic={}, offset={}, pair={}, score={}",
                        meta.topic(), meta.offset(), key, similarity.getScore());
            }
        });
    }

    public void flush() {
        producer.flush();
        Exception e = sendError.getAndSet(null);
        if (e != null) {
            throw new KafkaException("Ошибка при отправке сообщения в топик " + eventSimilarityTopic, e);
        }
    }

    @Override
    public void close() {
        producer.close(Duration.ofSeconds(10));
    }
}
