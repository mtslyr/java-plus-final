package ru.practicum.ewm.interaction.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

public interface RequestApi {

    @GetMapping("/internal/requests/confirmed")
    Map<Long, Long> getConfirmedCounts(@RequestParam("eventIds") Collection<Long> eventIds);

    @DeleteMapping("/internal/requests/users/{userId}")
    void deleteUserData(@PathVariable("userId") Long userId,
                        @RequestParam(value = "eventIds", required = false) Collection<Long> eventIds);
}
