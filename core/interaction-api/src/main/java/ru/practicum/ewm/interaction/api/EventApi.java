package ru.practicum.ewm.interaction.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.interaction.dto.EventInternalDto;

import java.util.List;

public interface EventApi {

    @GetMapping("/internal/events/{eventId}")
    EventInternalDto getById(@PathVariable Long eventId);

    @DeleteMapping("/internal/events/users/{userId}")
    List<Long> deleteUserData(@PathVariable Long userId);
}
