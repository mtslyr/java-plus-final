package ru.practicum.ewm.locations.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.locations.dto.LocationSearchRequest;
import ru.practicum.ewm.locations.services.LocationService;

import java.util.List;

@RestController
@RequestMapping(path = "/locations")
@RequiredArgsConstructor
public class PublicLocationController {

    private final LocationService locationService;

    @GetMapping
    public List<EventShortDto> getEventsInLocation(@Valid LocationSearchRequest request) {
        return locationService.getEventsInLocation(request);
    }
}