package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.interaction.api.RequestApi;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository.RequestCountView;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalRequestController implements RequestApi {

    private final ParticipationRequestRepository requestRepository;

    @Override
    public Map<Long, Long> getConfirmedCounts(Collection<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return requestRepository.countByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(RequestCountView::getEventId, RequestCountView::getCnt));
    }

    @Override
    @Transactional
    public void deleteUserData(Long userId, Collection<Long> eventIds) {
        requestRepository.deleteByRequesterId(userId);
        if (eventIds != null && !eventIds.isEmpty()) {
            requestRepository.deleteByEventIdIn(eventIds);
        }
    }
}
