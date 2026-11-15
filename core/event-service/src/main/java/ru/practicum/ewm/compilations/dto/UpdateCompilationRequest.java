package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class UpdateCompilationRequest {
    private List<Long> events;
    private Boolean pinned;
    @Size(min = 1, max = 50, message = "title should be shorter than 50")
    private String title;
}