package ru.practicum.ewm.request.service;

import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> getAllByUser(Long userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ParticipationRequestDto> getByEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeStatus(Long userId, Long eventId,
                                                  EventRequestStatusUpdateRequest dto);
}
