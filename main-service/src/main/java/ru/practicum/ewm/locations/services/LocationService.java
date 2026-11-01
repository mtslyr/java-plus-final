package ru.practicum.ewm.locations.services;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.locations.dto.LocationDtoResponse;
import ru.practicum.ewm.locations.dto.LocationSearchRequest;
import ru.practicum.ewm.locations.dto.NewLocationDto;
import ru.practicum.ewm.locations.dto.UpdateLocationRequest;

import java.util.List;

public interface LocationService {
    LocationDtoResponse create(NewLocationDto dto);

    LocationDtoResponse patch(Long locId, UpdateLocationRequest dto);

    void delete(Long locId);

    List<LocationDtoResponse> getAll(Pageable pageable);

    LocationDtoResponse getById(Long locId);

    List<EventShortDto> getEventsInLocation(LocationSearchRequest request);
}