package ru.practicum.ewm.stats.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.kafka.UserActionProducer;
import ru.practicum.ewm.stats.mapper.UserActionMapper;
import ru.practicum.grpc.recommendations.user.UserActionProto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionHandler {
    private final UserActionProducer producer;
    private final UserActionMapper mapper;

    public void handleUserAction(UserActionProto proto) {
        log.debug("Handle user action: {}", proto);
        UserActionAvro avro = mapper.mapToAvro(proto);
        producer.send(avro, avro.getUserId(), avro.getTimestamp());
    }

}
