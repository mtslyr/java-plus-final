package ru.practicum.ewm.locations.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class LocationSearchRequest {

    @NotNull(message = "lat is mandatory")
    @DecimalMin(value = "-90.0", message = "lat must be >= -90")
    @DecimalMax(value = "90.0", message = "lat must be <= 90")
    private Float lat;

    @NotNull(message = "lon is mandatory")
    @DecimalMin(value = "-180.0", message = "lon must be >= -180")
    @DecimalMax(value = "180.0", message = "lon must be <= 180")
    private Float lon;

    @NotNull(message = "radius is mandatory")
    @DecimalMin(value = "0.1", message = "radius must be > 0")
    private Float radius;
}
