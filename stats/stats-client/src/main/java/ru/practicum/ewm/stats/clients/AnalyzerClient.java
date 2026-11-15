package ru.practicum.ewm.stats.clients;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.recommendations.dashboard.InteractionsCountRequestProto;
import ru.practicum.grpc.recommendations.dashboard.RecommendedEventProto;
import ru.practicum.grpc.recommendations.dashboard.SimilarEventsRequestProto;
import ru.practicum.grpc.recommendations.dashboard.UserPredictionsRequestProto;
import ru.practicum.stats.service.dashboard.RecommendationsControllerGrpc;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class AnalyzerClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(Math.toIntExact(userId))
                .setMaxResults(maxResults)
                .build();
        return asStream(client.getRecommendationsForUser(request));
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(Math.toIntExact(eventId))
                .setUserId(Math.toIntExact(userId))
                .setMaxResults(maxResults)
                .build();
        return asStream(client.getSimilarEvents(request));
    }

    public Stream<RecommendedEventProto> getInteractionsCount(Collection<Long> eventIds) {
        InteractionsCountRequestProto.Builder request = InteractionsCountRequestProto.newBuilder();
        eventIds.forEach(id -> request.addEventId(Math.toIntExact(id)));
        return asStream(client.getInteractionsCount(request.build()));
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
