package ru.practicum.ewm.event.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {

    private Float lat;

    private Float lon;
}
