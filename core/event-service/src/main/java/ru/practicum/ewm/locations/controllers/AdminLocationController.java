package ru.practicum.ewm.locations.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.locations.dto.LocationDtoResponse;
import ru.practicum.ewm.locations.dto.NewLocationDto;
import ru.practicum.ewm.locations.dto.UpdateLocationRequest;
import ru.practicum.ewm.locations.services.LocationService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/locations")
@RequiredArgsConstructor
public class AdminLocationController {

    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDtoResponse create(@RequestBody @Valid NewLocationDto dto) {
        return locationService.create(dto);
    }

    @PatchMapping("/{locId}")
    public LocationDtoResponse patch(@PathVariable Long locId,
                                     @RequestBody @Valid UpdateLocationRequest dto) {
        return locationService.patch(locId, dto);
    }

    @DeleteMapping("/{locId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long locId) {
        locationService.delete(locId);
    }

    @GetMapping
    public List<LocationDtoResponse> getAll(Pageable pageable) {
        return locationService.getAll(pageable);
    }

    @GetMapping("/{locId}")
    public LocationDtoResponse getById(@PathVariable Long locId) {
        return locationService.getById(locId);
    }
}