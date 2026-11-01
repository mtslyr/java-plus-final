package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.exception.CategoryNotFoundException;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.common.util.EventUtil;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.exception.EventNotFoundException;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.EventSpecification;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.service.RatingService;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.ValidationException;
import ru.practicum.ewm.subscription.model.SubscriptionStatus;
import ru.practicum.ewm.subscription.repository.SubscriptionRepository;
import ru.practicum.ewm.user.exception.UserNotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

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
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CommentRepository commentRepository;
    private final EventUtil eventUtil;
    private final EventMapper mapper;
    private final RatingService ratingService;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategory()));

        LocalDateTime eventDate = LocalDateTime.parse(dto.getEventDate(), FORMATTER);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT))) {
            throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + dto.getEventDate());
        }

        Event event = mapper.toEvent(dto, category, user);
        event = eventRepository.save(event);
        log.debug("Event created: {} by user {}", event.getId(), userId);
        return mapper.toEventFullDto(event, 0L, 0L, RatingStatsDto.EMPTY, 0L);
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        PageRequest page = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, page).getContent();
        Map<Long, RatingStatsDto> ratings = getRatings(events);
        Map<Long, Long> commentCounts = getCommentCounts(events);
        return events.stream()
                .map(e -> mapper.toEventShortDto(e,
                        eventUtil.getViews(e.getId()),
                        eventUtil.getConfirmedRequests(e.getId()),
                        ratingFor(e.getId(), ratings),
                        countFor(e.getId(), commentCounts)))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getByUserAndEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new EventNotFoundException(eventId);
        }
        return mapper.toEventFullDto(event, eventUtil.getViews(eventId),
                eventUtil.getConfirmedRequests(eventId), getRating(eventId), getCommentCount(eventId));
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getInitiator().getId().equals(userId)) {
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
        return mapper.toEventFullDto(event, eventUtil.getViews(eventId),
                eventUtil.getConfirmedRequests(eventId), getRating(eventId), getCommentCount(eventId));
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
        Map<Long, RatingStatsDto> ratings = getRatings(events);
        Map<Long, Long> commentCounts = getCommentCounts(events);
        return events.stream()
                .map(e -> mapper.toEventFullDto(e, eventUtil.getViews(e.getId()),
                        eventUtil.getConfirmedRequests(e.getId()), ratingFor(e.getId(), ratings),
                        countFor(e.getId(), commentCounts)))
                .collect(Collectors.toList());
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
        return mapper.toEventFullDto(event, eventUtil.getViews(eventId),
                eventUtil.getConfirmedRequests(eventId), getRating(eventId), getCommentCount(eventId));
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

        Map<Long, RatingStatsDto> ratings = getRatings(events);
        Map<Long, Long> commentCounts = getCommentCounts(events);
        List<EventShortDto> result = new ArrayList<>();
        List<EventShortDto> subscribedEvents = new ArrayList<>();
        List<EventShortDto> otherEvents = new ArrayList<>();

        for (Event e : events) {
            long confirmed = eventUtil.getConfirmedRequests(e.getId());
            if (Boolean.TRUE.equals(onlyAvailable) && e.getParticipantLimit() != 0
                    && confirmed >= e.getParticipantLimit()) {
                continue;
            }
            EventShortDto dto = mapper.toEventShortDto(e, eventUtil.getViews(e.getId()), confirmed,
                    ratingFor(e.getId(), ratings), countFor(e.getId(), commentCounts));
            if (isUserSubscribed(userId, e.getInitiator().getId())) {
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

    private boolean isUserSubscribed(Long userId, Long initiatorId) {
        if (userId == null) {
            return false;
        }
        return subscriptionRepository.existsByFollowerIdAndFollowedIdAndStatus(userId, initiatorId, SubscriptionStatus.CONFIRMED);
    }

    @Override
    public EventFullDto getPublishedById(Long id) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(id));

        return mapper.toEventFullDto(event, eventUtil.getViews(id),
                eventUtil.getConfirmedRequests(id), getRating(id), getCommentCount(id));
    }

    private Map<Long, RatingStatsDto> getRatings(Collection<Event> events) {
        return ratingService.getStats(events.stream().map(Event::getId).toList());
    }

    private RatingStatsDto getRating(Long eventId) {
        return ratingFor(eventId, ratingService.getStats(List.of(eventId)));
    }

    private RatingStatsDto ratingFor(Long eventId, Map<Long, RatingStatsDto> ratings) {
        return ratings.getOrDefault(eventId, RatingStatsDto.EMPTY);
    }

    private Map<Long, Long> getCommentCounts(Collection<Event> events) {
        List<Long> ids = events.stream().map(Event::getId).toList();
        if (ids.isEmpty()) return Collections.emptyMap();
        return commentRepository.countByEventIdIn(ids).stream()
                .collect(Collectors.toMap(
                        view -> view.getEventId(),
                        view -> view.getCnt()
                ));
    }

    private Long getCommentCount(Long eventId) {
        return countFor(eventId, commentRepository.countByEventIdIn(List.of(eventId)).stream()
                .collect(Collectors.toMap(
                        view -> view.getEventId(),
                        view -> view.getCnt()
                )));
    }

    private Long countFor(Long eventId, Map<Long, Long> commentCounts) {
        return commentCounts.getOrDefault(eventId, 0L);
    }
}
