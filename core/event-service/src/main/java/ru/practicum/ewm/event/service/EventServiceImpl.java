package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.exception.CategoryNotFoundException;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.common.exception.EventNotFoundException;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.interaction.dto.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.EventSpecification;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.ValidationException;
import ru.practicum.ewm.common.exception.UserNotFoundException;
import ru.practicum.ewm.interaction.api.UserApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final String PUBLISH_EVENT = "PUBLISH_EVENT";
    private static final String REJECT_EVENT = "REJECT_EVENT";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long HOURS_BEFORE_EVENT = 2;

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserApi userApi;
    private final EventEnricher enricher;
    private final EventMapper mapper;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        checkUserExists(userId);

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));

        LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT))) {
            throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
        }

        Event event = mapper.toEvent(dto, category, userId);
        event = eventRepository.save(event);
        log.debug("Event created: {} by user {}", event.getId(), userId);
        return enricher.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        checkUserExists(userId);

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, page).getContent();
        return enricher.toShortDtos(events);
    }

    @Override
    public EventFullDto getByUserAndEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!event.getInitiatorId().equals(userId)) {
            throw new EventNotFoundException(eventId);
        }
        return enricher.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getInitiatorId().equals(userId)) {
            throw new EventNotFoundException(eventId);
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (dto.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
            if (newDate.isBefore(LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT))) {
                throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
            }
        }

        Category category = null;
        if (dto.getCategory() != null) {
            category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));
        }

        EventMapper.updateEventFromUserRequest(event, dto, category);

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ValidationException("Unknown state action: " + dto.getStateAction());
            }
        }

        event = eventRepository.save(event);
        log.debug("Event updated by user: {}", eventId);
        return enricher.toFullDto(event);
    }

    @Override
    public List<EventFullDto> getByAdmin(List<Long> users, List<String> states, List<Long> categories,
                                         String rangeStart, String rangeEnd, Integer from, Integer size) {
        List<EventState> stateEnums = null;
        if (states != null && !states.isEmpty()) {
            stateEnums = states.stream().map(EventState::valueOf).collect(Collectors.toList());
        }

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;

        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Field: rangeStart. Error: start must be before end. Value: rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd);
        }

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(
                EventSpecification.eventsByAdmin(users, stateEnums, categories, start, end),
                page
        ).getContent();
        return enricher.toFullDtos(events);
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        Category category = null;
        if (dto.getCategory() != null) {
            category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));
        }

        if (dto.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
            if (newDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
            }
        }

        EventMapper.updateEventFromAdminRequest(event, dto, category);

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Event date must be at least 1 hour from now");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject the event because it's already published");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ValidationException("Unknown state action: " + dto.getStateAction());
            }
        }

        event = eventRepository.save(event);
        log.debug("Event updated by admin: {}", eventId);
        return enricher.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getPublished(String text, List<Long> categories, Boolean paid,
                                            String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                            String sort, Integer from, Integer size, Long userId) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, FORMATTER) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, FORMATTER) : null;

        if (start != null && end != null && start.isAfter(end)) {
            throw new ValidationException("Field: rangeStart. Error: start must be before end. Value: rangeStart=" + rangeStart + ", rangeEnd=" + rangeEnd);
        }

        if (start == null && end == null) {
            start = LocalDateTime.now();
        }

        boolean commentsSort = "COMMENTS".equals(sort);
        boolean ratingSort = "RATING".equals(sort);
        boolean noPaginationSort = commentsSort || ratingSort;
        List<Event> events;
        if (noPaginationSort) {
            events = eventRepository.findAll(
                    EventSpecification.publishedEvents(text, categories, paid, start, end)
            );
        } else {
            PageRequest page = PageRequest.of(from / size, size);
            events = eventRepository.findAll(
                    EventSpecification.publishedEvents(text, categories, paid, start, end),
                    page
            ).getContent();
        }

        List<EventShortDto> dtos = enricher.toShortDtos(events);
        Set<Long> followedIds = getFollowedInitiatorIds(userId, events);
        List<EventShortDto> result = new ArrayList<>();
        List<EventShortDto> subscribedEvents = new ArrayList<>();
        List<EventShortDto> otherEvents = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            EventShortDto dto = dtos.get(i);
            if (Boolean.TRUE.equals(onlyAvailable) && e.getParticipantLimit() != 0
                    && dto.getConfirmedRequests() >= e.getParticipantLimit()) {
                continue;
            }
            if (followedIds.contains(e.getInitiatorId())) {
                subscribedEvents.add(dto);
            } else {
                otherEvents.add(dto);
            }
        }

        if (commentsSort) {
            subscribedEvents.sort(Comparator.comparingLong(EventShortDto::getCommentCount).reversed());
            otherEvents.sort(Comparator.comparingLong(EventShortDto::getCommentCount).reversed());
        } else if (ratingSort) {
            Comparator<EventShortDto> byRating =
                    Comparator.comparingLong(EventShortDto::getRating).reversed();
            subscribedEvents.sort(byRating);
            otherEvents.sort(byRating);
        } else if ("VIEWS".equals(sort)) {
            subscribedEvents.sort(Comparator.comparingLong(EventShortDto::getViews));
            otherEvents.sort(Comparator.comparingLong(EventShortDto::getViews));
        }

        result.addAll(subscribedEvents);
        result.addAll(otherEvents);

        if (noPaginationSort) {
            int startIndex = Math.min(from, result.size());
            int endIndex = Math.min(startIndex + size, result.size());
            return new ArrayList<>(result.subList(startIndex, endIndex));
        }

        return result;
    }

    private Set<Long> getFollowedInitiatorIds(Long userId, List<Event> events) {
        if (userId == null) {
            return Set.of();
        }
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().toList();
        return userApi.getFollowedIds(userId, initiatorIds);
    }

    @Override
    public EventFullDto getPublishedById(Long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(id));

        return enricher.toFullDto(event);
    }

    private void checkUserExists(Long userId) {
        if (!userApi.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }
}
