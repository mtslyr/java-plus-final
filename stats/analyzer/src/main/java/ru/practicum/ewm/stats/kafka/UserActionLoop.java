package ru.practicum.ewm.stats.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.configuration.KafkaTopicProperties;
import ru.practicum.ewm.stats.configuration.WeightsConfig;
import ru.practicum.ewm.stats.repository.UserActionWeightRepository;

@Slf4j
@Component
public class UserActionLoop extends KafkaPollingLoop<UserActionAvro> {

    private final UserActionWeightRepository repository;
    private final WeightsConfig weightsConfig;

    public UserActionLoop(KafkaConsumer<String, UserActionAvro> userActionConsumer,
                          KafkaTopicProperties topics,
                          UserActionWeightRepository repository,
                          WeightsConfig weightsConfig) {
        super(userActionConsumer, topics.userActions());
        this.repository = repository;
        this.weightsConfig = weightsConfig;
    }

    @Override
    protected void handle(UserActionAvro action) {
        double weight = weightsConfig.weightOf(action.getActionType());
        repository.upsertMaxWeight(action.getUserId(), action.getEventId(), weight, action.getTimestamp());
        log.debug("Сохранено действие: userId={}, eventId={}, weight={}",
                action.getUserId(), action.getEventId(), weight);
    }
}
