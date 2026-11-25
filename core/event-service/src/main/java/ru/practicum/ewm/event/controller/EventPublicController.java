package ru.practicum.ewm.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventPublicController {

    private static final String USER_ID_HEADER = "X-EWM-USER-ID";

    private final EventService eventService;

    @GetMapping("/events")
    public List<EventShortDto> getPublished(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd,
                                            @RequestParam(required = false) Boolean onlyAvailable,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestHeader(value = "X-EWM-UserId", required = false) Long userId) {
        return eventService.getPublished(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, userId);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getPublishedById(@PathVariable Long id,
                                         @RequestHeader(USER_ID_HEADER) Long userId) {
        return eventService.getPublishedById(id, userId);
    }

    @GetMapping("/events/recommendations")
    public List<EventShortDto> getRecommendations(@RequestHeader(USER_ID_HEADER) Long userId,
                                                  @RequestParam(defaultValue = "10") Integer maxResults) {
        return eventService.getRecommendations(userId, maxResults);
    }

    @PutMapping("/events/{eventId}/like")
    public void like(@PathVariable Long eventId,
                     @RequestHeader(USER_ID_HEADER) Long userId) {
        eventService.like(userId, eventId);
    }
}
