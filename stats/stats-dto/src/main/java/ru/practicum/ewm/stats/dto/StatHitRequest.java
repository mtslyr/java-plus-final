package ru.practicum.ewm.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.stats.validation.OnCreate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatHitRequest {
    @NotBlank(
            groups = OnCreate.class,
            message = "Application name is required")
    @JsonProperty("app")
    String appName;

    @NotBlank(
            groups = OnCreate.class,
            message = "Requested URI is required"
    )
    String uri;

    @NotBlank(
            groups = OnCreate.class,
            message = "Requester IP is required"
    )
    String ip;

    @NotNull(
            groups = OnCreate.class,
            message = "Request timestamp is required"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;
}
