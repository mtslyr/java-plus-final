package ru.practicum.ewm.compilations.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.common.util.EventUtil;
import ru.practicum.ewm.compilations.dto.CompilationDtoResponse;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.models.Compilation;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.service.RatingService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventUtil eventUtil;
    private final CommentRepository commentRepository;
    private final RatingService ratingService;

    public Compilation toModel(NewCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

    public CompilationDtoResponse toDto(Compilation model) {
        List<Long> eventIds = model.getEvents().stream()
                .map(ce -> ce.getEvent().getId())
                .toList();
        Map<Long, RatingStatsDto> ratings = ratingService.getStats(eventIds);
        Map<Long, Long> commentCounts = eventIds.isEmpty() ? Collections.emptyMap()
                : commentRepository.countByEventIdIn(eventIds).stream()
                .collect(Collectors.toMap(
                        view -> view.getEventId(),
                        view -> view.getCnt()
                ));
        return CompilationDtoResponse.builder()
                .events(model.getEvents().stream()
                        .map(event
                                -> EventMapper.toEventShortDto(
                                event.getEvent(),
                                eventUtil.getViews(event.getEvent().getId()),
                                eventUtil.getConfirmedRequests(event.getEvent().getId()),
                                ratings.getOrDefault(event.getEvent().getId(), RatingStatsDto.EMPTY),
                                commentCounts.getOrDefault(event.getEvent().getId(), 0L)))
                        .toList())
                .id(model.getId())
                .pinned(model.getPinned())
                .title(model.getTitle())
                .build();

    }
}
