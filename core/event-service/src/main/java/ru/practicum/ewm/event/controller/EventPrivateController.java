package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllByUser(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getAllByUser(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto dto) {
        return eventService.create(userId, dto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getByUserAndEvent(@PathVariable Long userId,
                                          @PathVariable Long eventId) {
        return eventService.getByUserAndEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateByUser(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     @Valid @RequestBody UpdateEventUserRequest dto) {
        return eventService.updateByUser(userId, eventId, dto);
    }
}
