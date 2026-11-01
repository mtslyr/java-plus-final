package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.event.exception.EventNotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.exception.RequestNotFoundException;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.exception.UserNotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper mapper;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot add request to own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exists");
        }

        if (event.getParticipantLimit() != 0) {
            long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmed >= event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached");
            }
        }

        RequestStatus status = event.getParticipantLimit() != 0 && event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED;

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();

        request = requestRepository.save(request);
        log.debug("Request created: {} for event {}", request.getId(), eventId);
        return mapper.toRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getAllByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return requestRepository.findByRequesterId(userId).stream()
                .map(mapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!request.getRequester().getId().equals(userId)) {
            throw new RequestNotFoundException(requestId);
        }

        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);
        log.debug("Request canceled: {}", requestId);
        return mapper.toRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getByEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new EventNotFoundException(eventId);
        }

        return requestRepository.findByEventId(eventId).stream()
                .map(mapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeStatus(Long userId, Long eventId,
                                                         EventRequestStatusUpdateRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new EventNotFoundException(eventId);
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Confirmation of requests is not required");
        }

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();

        List<ParticipationRequest> requests = requestRepository.findByIdIn(dto.getRequestIds());
        if (requests.size() != dto.getRequestIds().size()) {
            Collection<Long> notFound = CollectionUtils.subtract(
                    dto.getRequestIds(),
                    requests.stream().map(ParticipationRequest::getId).toList());
            throw new RequestNotFoundException(notFound);
        }

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }

            if ("CONFIRMED".equals(dto.getStatus())) {
                if (confirmed >= limit) {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(mapper.toRequestDto(request));
                    continue;
                }
                request.setStatus(RequestStatus.CONFIRMED);
                confirmed++;
            } else {
                request.setStatus(RequestStatus.REJECTED);
            }
        }

        requestRepository.saveAll(requests);

        for (ParticipationRequest request : requests) {
            if (request.getStatus() == RequestStatus.CONFIRMED) {
                confirmedRequests.add(mapper.toRequestDto(request));
            } else {
                rejectedRequests.add(mapper.toRequestDto(request));
            }
        }

        if (confirmed >= limit) {
            List<ParticipationRequest> pending = requestRepository.findByEventId(eventId).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .toList();
            for (ParticipationRequest r : pending) {
                r.setStatus(RequestStatus.REJECTED);
            }
            requestRepository.saveAll(pending);
            rejectedRequests.addAll(pending.stream().map(mapper::toRequestDto).toList());
        }

        log.debug("Requests confirmed: {}, rejected: {}", confirmedRequests.size(), rejectedRequests.size());
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }
}
