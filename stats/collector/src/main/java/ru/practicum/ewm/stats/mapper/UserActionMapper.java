package ru.practicum.ewm.stats.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.recommendations.user.ActionTypeProto;
import ru.practicum.grpc.recommendations.user.UserActionProto;

import java.time.Instant;

@Component
public class UserActionMapper {
    public UserActionAvro mapToAvro(UserActionProto proto) {
        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(mapActionType(proto.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(
                        proto.getTimestamp().getSeconds(),
                        proto.getTimestamp().getNanos()))
                .build();
    }

    public ActionTypeAvro mapActionType(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            default -> throw new IllegalArgumentException("Тип действия не определен для %s.%s"
                    .formatted(proto.getClass(), proto));
        };
    }
}
