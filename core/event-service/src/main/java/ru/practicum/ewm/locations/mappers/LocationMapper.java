package ru.practicum.ewm.locations.mappers;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.locations.dto.LocationDtoResponse;
import ru.practicum.ewm.locations.dto.NewLocationDto;
import ru.practicum.ewm.locations.dto.UpdateLocationRequest;
import ru.practicum.ewm.locations.models.Location;

@Component
public class LocationMapper {

    public Location toModel(NewLocationDto dto, Event event) {
        return Location.builder()
                .event(event)
                .name(dto.getName())
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    public LocationDtoResponse toDto(Location model) {
        return LocationDtoResponse.builder()
                .id(model.getId())
                .eventId(model.getEvent().getId())
                .name(model.getName())
                .lat(model.getLat())
                .lon(model.getLon())
                .build();
    }

    public void update(UpdateLocationRequest dto, Location location, Event newEvent) {
        if (dto.getEventId() != null && newEvent != null) {
            location.setEvent(newEvent);
        }
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            location.setName(dto.getName());
        }
        if (dto.getLat() != null) {
            location.setLat(dto.getLat());
        }
        if (dto.getLon() != null) {
            location.setLon(dto.getLon());
        }
    }
}