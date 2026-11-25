package ru.practicum.ewm.stats.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.configuration.KafkaTopicProperties;
import ru.practicum.ewm.stats.repository.EventSimilarityRepository;

@Slf4j
@Component
public class EventSimilarityLoop extends KafkaPollingLoop<String, EventSimilarityAvro> {

    private final EventSimilarityRepository repository;

    public EventSimilarityLoop(KafkaConsumer<String, EventSimilarityAvro> eventSimilarityConsumer,
                               KafkaTopicProperties topics,
                               EventSimilarityRepository repository) {
        super(eventSimilarityConsumer, topics.eventSimilarity());
        this.repository = repository;
    }

    @Override
    protected void handle(EventSimilarityAvro similarity) {
        repository.upsert(similarity.getEventA(), similarity.getEventB(),
                similarity.getScore(), similarity.getTimestamp());
        log.debug("Сохранено сходство: ({}, {}) score={}",
                similarity.getEventA(), similarity.getEventB(), similarity.getScore());
    }
}
