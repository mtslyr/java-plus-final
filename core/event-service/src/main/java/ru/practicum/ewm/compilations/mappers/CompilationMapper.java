package ru.practicum.ewm.compilations.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.compilations.dto.CompilationDtoResponse;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.models.Compilation;
import ru.practicum.ewm.compilations.models.CompilationEvent;
import ru.practicum.ewm.event.service.EventEnricher;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventEnricher eventEnricher;

    public Compilation toModel(NewCompilationDto dto) {
        return Compilation.builder()
                .pinned(dto.getPinned())
                .title(dto.getTitle())
                .build();
    }

    public CompilationDtoResponse toDto(Compilation model) {
        return CompilationDtoResponse.builder()
                .events(eventEnricher.toShortDtos(model.getEvents().stream()
                        .map(CompilationEvent::getEvent)
                        .toList()))
                .id(model.getId())
                .pinned(model.getPinned())
                .title(model.getTitle())
                .build();
    }
}
