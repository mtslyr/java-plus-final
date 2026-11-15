package ru.practicum.ewm.compilations.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@Builder
@Getter
@ToString
public class CompilationDtoResponse {
    private Long id;
    private Boolean pinned;
    private String title;
    private List<EventShortDto> events;
}