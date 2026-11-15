package ru.practicum.ewm.locations.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class LocationDtoResponse {
    private Long id;
    private Long eventId;
    private String name;
    private Float lat;
    private Float lon;
}
