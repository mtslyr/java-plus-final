package ru.practicum.ewm.locations.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Builder
@Getter
@ToString
public class NewLocationDto {

    @NotNull(message = "eventId is mandatory")
    private Long eventId;

    @Size(max = 100, message = "name should be shorter than 100")
    private String name;

    @NotNull(message = "lat is mandatory")
    @DecimalMin(value = "-90.0", message = "lat must be >= -90")
    @DecimalMax(value = "90.0", message = "lat must be <= 90")
    private Float lat;

    @NotNull(message = "lon is mandatory")
    @DecimalMin(value = "-180.0", message = "lon must be >= -180")
    @DecimalMax(value = "180.0", message = "lon must be <= 180")
    private Float lon;
}
