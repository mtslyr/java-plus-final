package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.util.EventUtil;
import ru.practicum.ewm.interaction.api.CommentApi;
import ru.practicum.ewm.interaction.api.RequestApi;
import ru.practicum.ewm.interaction.api.UserApi;
import ru.practicum.ewm.interaction.dto.UserShortDto;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.service.RatingService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventEnricher {

    private final EventMapper mapper;
    private final EventUtil eventUtil;
    private final RatingService ratingService;
    private final UserApi userApi;
    private final RequestApi requestApi;
    private final CommentApi commentApi;

    public EventFullDto toFullDto(Event event) {
        return toFullDtos(List.of(event)).getFirst();
    }

    public List<EventFullDto> toFullDtos(List<Event> events) {
        EventStats stats = collectStats(events);
        return events.stream()
                .map(e -> mapper.toEventFullDto(e, stats.initiator(e), eventUtil.getViews(e.getId()),
                        stats.confirmed(e), stats.rating(e), stats.comments(e)))
                .toList();
    }

    public List<EventShortDto> toShortDtos(List<Event> events) {
        EventStats stats = collectStats(events);
        return events.stream()
                .map(e -> mapper.toEventShortDto(e, stats.initiator(e), eventUtil.getViews(e.getId()),
                        stats.confirmed(e), stats.rating(e), stats.comments(e)))
                .toList();
    }

    private EventStats collectStats(Collection<Event> events) {
        if (events.isEmpty()) {
            return new EventStats(Map.of(), Map.of(), Map.of(), Map.of());
        }
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        Map<Long, UserShortDto> initiators = userApi.getShortByIds(initiatorIds).stream()
                .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
        return new EventStats(
                initiators,
                requestApi.getConfirmedCounts(eventIds),
                ratingService.getStats(eventIds),
                commentApi.getCommentCounts(eventIds));
    }

    private record EventStats(Map<Long, UserShortDto> initiators,
                              Map<Long, Long> confirmedRequests,
                              Map<Long, RatingStatsDto> ratings,
                              Map<Long, Long> commentCounts) {

        UserShortDto initiator(Event event) {
            return initiators.getOrDefault(event.getInitiatorId(),
                    new UserShortDto(event.getInitiatorId(), null));
        }

        Long confirmed(Event event) {
            return confirmedRequests.getOrDefault(event.getId(), 0L);
        }

        RatingStatsDto rating(Event event) {
            return ratings.getOrDefault(event.getId(), RatingStatsDto.EMPTY);
        }

        Long comments(Event event) {
            return commentCounts.getOrDefault(event.getId(), 0L);
        }
    }
}
