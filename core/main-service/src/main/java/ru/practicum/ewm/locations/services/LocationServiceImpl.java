package ru.practicum.ewm.locations.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.common.exception.LocationNotFoundException;
import ru.practicum.ewm.common.util.EventUtil;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.exception.EventNotFoundException;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.locations.dto.LocationDtoResponse;
import ru.practicum.ewm.locations.dto.LocationSearchRequest;
import ru.practicum.ewm.locations.dto.NewLocationDto;
import ru.practicum.ewm.locations.dto.UpdateLocationRequest;
import ru.practicum.ewm.locations.mappers.LocationMapper;
import ru.practicum.ewm.locations.models.Location;
import ru.practicum.ewm.locations.repository.LocationRepository;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.service.RatingService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final LocationMapper mapper;
    private final EventUtil eventUtil;
    private final RatingService ratingService;

    @Override
    @Transactional
    public LocationDtoResponse create(NewLocationDto dto) {
        log.debug("LocationService->create: {}", dto);
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new EventNotFoundException(dto.getEventId()));
        Location location = mapper.toModel(dto, event);
        Location saved = locationRepository.save(location);
        log.debug("LocationService->create result: {}", saved);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public LocationDtoResponse patch(Long locId, UpdateLocationRequest dto) {
        log.debug("LocationService->patch id={}. {}", locId, dto);
        Location location = locationRepository.findById(locId)
                .orElseThrow(() -> new LocationNotFoundException(locId));

        Event newEvent = null;
        if (dto.getEventId() != null) {
            newEvent = eventRepository.findById(dto.getEventId())
                    .orElseThrow(() -> new EventNotFoundException(dto.getEventId()));
        }

        mapper.update(dto, location, newEvent);
        Location saved = locationRepository.save(location);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long locId) {
        log.debug("LocationService->delete id={}", locId);
        Location location = locationRepository.findById(locId)
                .orElseThrow(() -> new LocationNotFoundException(locId));
        locationRepository.delete(location);
    }

    @Override
    public List<LocationDtoResponse> getAll(Pageable pageable) {
        log.debug("LocationService->getAll pageable={}", pageable);
        return locationRepository.findAllWithOffset(pageable)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public LocationDtoResponse getById(Long locId) {
        log.debug("LocationService->getById id={}", locId);
        Location location = locationRepository.findById(locId)
                .orElseThrow(() -> new LocationNotFoundException(locId));
        return mapper.toDto(location);
    }

    @Override
    public List<EventShortDto> getEventsInLocation(LocationSearchRequest request) {
        log.debug("LocationService->getEventsInLocation: lat={}, lon={}, radius={}",
                request.getLat(), request.getLon(), request.getRadius());

        List<Long> eventIds = locationRepository.findEventIdsWithinRadius(
                request.getLat(), request.getLon(), request.getRadius());

        if (eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllById(eventIds);

        Map<Long, RatingStatsDto> ratings = ratingService.getStats(eventIds);
        Map<Long, Long> commentCounts = getCommentCounts(eventIds);

        return events.stream()
                .map(event -> EventMapper.toEventShortDto(
                        event,
                        eventUtil.getViews(event.getId()),
                        eventUtil.getConfirmedRequests(event.getId()),
                        ratings.getOrDefault(event.getId(), RatingStatsDto.EMPTY),
                        commentCounts.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    private Map<Long, Long> getCommentCounts(List<Long> events) {
        if (events.isEmpty()) return Collections.emptyMap();
        return commentRepository.countByEventIdIn(events).stream()
                .collect(Collectors.toMap(
                        view -> view.getEventId(),
                        view -> view.getCnt()
                ));
    }
}
