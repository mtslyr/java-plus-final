package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.interaction.api.CommentApi;
import ru.practicum.ewm.interaction.api.RequestApi;
import ru.practicum.ewm.interaction.api.UserApi;
import ru.practicum.ewm.interaction.dto.UserShortDto;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.service.RatingService;
import ru.practicum.ewm.stats.clients.AnalyzerClient;
import ru.practicum.grpc.recommendations.dashboard.RecommendedEventProto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEnricher {

    private final EventMapper mapper;
    private final RatingService ratingService;
    private final UserApi userApi;
    private final RequestApi requestApi;
    private final CommentApi commentApi;
    private final AnalyzerClient analyzerClient;

    public EventFullDto toFullDto(Event event) {
        return toFullDtos(List.of(event)).getFirst();
    }

    public List<EventFullDto> toFullDtos(List<Event> events) {
        EventStats stats = collectStats(events);
        return events.stream()
                .map(e -> mapper.toEventFullDto(e, stats.initiator(e), stats.confirmed(e),
                        stats.ratingStats(e), stats.rating(e), stats.comments(e)))
                .toList();
    }

    public List<EventShortDto> toShortDtos(List<Event> events) {
        EventStats stats = collectStats(events);
        return events.stream()
                .map(e -> mapper.toEventShortDto(e, stats.initiator(e), stats.confirmed(e),
                        stats.ratingStats(e), stats.rating(e), stats.comments(e)))
                .toList();
    }

    private EventStats collectStats(Collection<Event> events) {
        if (events.isEmpty()) {
            return new EventStats(Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        Map<Long, UserShortDto> initiators = userApi.getShortByIds(initiatorIds).stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
        return new EventStats(
                initiators,
                requestApi.getConfirmedCounts(eventIds),
                ratingService.getStats(eventIds),
                getRatings(eventIds),
                commentApi.getCommentCounts(eventIds));
    }

    private Map<Long, Double> getRatings(List<Long> eventIds) {
        try {
            return analyzerClient.getInteractionsCount(eventIds)
                    .collect(Collectors.toMap(e -> (long) e.getEventId(), RecommendedEventProto::getScore));
        } catch (RuntimeException e) {
            log.warn("Failed to get ratings for events {}: {}", eventIds, e.getMessage());
            return Map.of();
        }
    }

    private record EventStats(Map<Long, UserShortDto> initiators,
                              Map<Long, Long> confirmedRequests,
                              Map<Long, RatingStatsDto> ratingStats,
                              Map<Long, Double> ratings,
                              Map<Long, Long> commentCounts) {

        UserShortDto initiator(Event event) {
            return initiators.getOrDefault(event.getInitiatorId(),
                    new UserShortDto(event.getInitiatorId(), null));
        }

        Long confirmed(Event event) {
            return confirmedRequests.getOrDefault(event.getId(), 0L);
        }

        RatingStatsDto ratingStats(Event event) {
            return ratingStats.getOrDefault(event.getId(), RatingStatsDto.EMPTY);
        }

        Double rating(Event event) {
            return ratings.getOrDefault(event.getId(), 0.0);
        }

        Long comments(Event event) {
            return commentCounts.getOrDefault(event.getId(), 0L);
        }
    }
}
