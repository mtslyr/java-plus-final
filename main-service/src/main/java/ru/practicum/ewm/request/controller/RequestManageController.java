package ru.practicum.ewm.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class RequestManageController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getByEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId) {
        return requestService.getByEvent(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult changeStatus(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @Valid @RequestBody EventRequestStatusUpdateRequest dto) {
        return requestService.changeStatus(userId, eventId, dto);
    }
}
