package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.common.exception.EventNotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.interaction.api.EventApi;
import ru.practicum.ewm.interaction.dto.EventInternalDto;
import ru.practicum.ewm.rating.repository.EventRatingRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalEventController implements EventApi {

    private final EventRepository eventRepository;
    private final EventRatingRepository ratingRepository;

    @Override
    public EventInternalDto getById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        return EventInternalDto.builder()
                .id(event.getId())
                .initiatorId(event.getInitiatorId())
                .state(event.getState())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .build();
    }

    @Override
    @Transactional
    public List<Long> deleteUserData(Long userId) {
        ratingRepository.deleteByUserId(userId);
        List<Long> eventIds = eventRepository.findIdsByInitiatorId(userId);
        if (!eventIds.isEmpty()) {
            eventRepository.deleteAllByIdInBatch(eventIds);
        }
        return eventIds;
    }
}
