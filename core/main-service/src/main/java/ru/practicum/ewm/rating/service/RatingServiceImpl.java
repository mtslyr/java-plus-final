package ru.practicum.ewm.rating.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.event.exception.EventNotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.rating.dto.RatingStatsDto;
import ru.practicum.ewm.rating.model.EventRating;
import ru.practicum.ewm.rating.model.RatingType;
import ru.practicum.ewm.rating.repository.EventRatingRepository;
import ru.practicum.ewm.user.exception.UserNotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final EventRatingRepository ratingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void setRating(Long userId, Long eventId, RatingType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Only published events can be rated");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("An initiator cannot rate their own event");
        }

        EventRating rating = ratingRepository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> EventRating.builder().event(event).user(user).build());
        rating.setType(type);
        ratingRepository.save(rating);
    }

    @Override
    @Transactional
    public void deleteRating(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        ratingRepository.findByEventIdAndUserId(eventId, userId)
                .ifPresent(ratingRepository::delete);
    }

    @Override
    public Map<Long, RatingStatsDto> getStats(Collection<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return ratingRepository.findStatsByEventIds(eventIds).stream()
                .collect(Collectors.toMap(
                        view -> view.getEventId(),
                        view -> new RatingStatsDto(view.getLikes(), view.getDislikes())
                ));
    }
}
