package ru.practicum.ewm.stats.clients;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.recommendations.user.ActionTypeProto;
import ru.practicum.grpc.recommendations.user.UserActionProto;
import ru.practicum.stats.service.collector.UserActionControllerGrpc;

import java.time.Instant;

@Slf4j
@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void sendView(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    public void sendRegister(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    public void sendLike(long userId, long eventId) {
        send(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    private void send(long userId, long eventId, ActionTypeProto actionType) {
        Instant now = Instant.now();
        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(Math.toIntExact(userId))
                .setEventId(Math.toIntExact(eventId))
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano()))
                .build();

        client.collectUserAction(action);
        log.debug("Отправлено действие: userId={}, eventId={}, type={}", userId, eventId, actionType);
    }
}
