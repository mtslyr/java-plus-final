package ru.practicum.ewm.stats.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.model.RecommendedEvent;
import ru.practicum.ewm.stats.service.RecommendationService;
import ru.practicum.grpc.recommendations.dashboard.InteractionsCountRequestProto;
import ru.practicum.grpc.recommendations.dashboard.RecommendedEventProto;
import ru.practicum.grpc.recommendations.dashboard.SimilarEventsRequestProto;
import ru.practicum.grpc.recommendations.dashboard.UserPredictionsRequestProto;
import ru.practicum.stats.service.dashboard.RecommendationsControllerGrpc;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        stream(responseObserver, () -> recommendationService.getRecommendationsForUser(
                request.getUserId(), request.getMaxResults()));
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        stream(responseObserver, () -> recommendationService.getSimilarEvents(
                request.getEventId(), request.getUserId(), request.getMaxResults()));
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        stream(responseObserver, () -> recommendationService.getInteractionsCount(request.getEventIdList()));
    }

    private void stream(StreamObserver<RecommendedEventProto> responseObserver,
                        Supplier<List<RecommendedEvent>> events) {
        try {
            for (RecommendedEvent event : events.get()) {
                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(event.eventId())
                        .setScore(event.score())
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn("Ошибка при обработке запроса: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}
