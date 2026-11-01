package ru.practicum.ewm.compilations.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilations.dto.CompilationDtoResponse;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilations.mappers.CompilationMapper;
import ru.practicum.ewm.compilations.models.Compilation;
import ru.practicum.ewm.compilations.models.CompilationEvent;
import ru.practicum.ewm.compilations.repository.CompilationRepository;
import ru.practicum.ewm.common.exception.CompilationNotFoundException;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final EventRepository eventRepository;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper mapper;

    @Override
    @Transactional
    public CompilationDtoResponse create(NewCompilationDto dto) {
        log.debug("CompilationService->create: {}", dto);
        List<Event> events = eventRepository.findByIdIn(dto.getEvents());

        Compilation compilation = mapper.toModel(dto);

        if (compilation.getEvents() == null) {
            compilation.setEvents(new ArrayList<>());
        }

        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }

        createCompilationEvents(compilation, events);

        Compilation result = compilationRepository.save(compilation);

        log.debug("CompilationService->create result: {}", result);
        return mapper.toDto(result);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        log.debug("CompilationService->delete id={}", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));
        compilationRepository.delete(compilation);
    }

    @Override
    @Transactional
    public CompilationDtoResponse patch(Long compId, UpdateCompilationRequest dto) {
        log.debug("CompilationService->patch id={}. {}", compId, dto);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(()
                        -> new CompilationNotFoundException(compId));

        if (dto.getEvents() != null) {
            compilation.getEvents().clear();
            List<Event> events = eventRepository
                    .findByIdIn(dto.getEvents());
            createCompilationEvents(compilation, events);
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            compilation.setTitle(dto.getTitle());
        }

        Compilation result = compilationRepository.save(compilation);
        return mapper.toDto(result);
    }

    @Override
    public List<CompilationDtoResponse> getCompilations(Boolean pinned, Pageable pageable) {
        log.debug("CompilationService->get pinned={}, pageable={}", pinned, pageable);
        return compilationRepository.findWithOffset(pinned, pageable)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public CompilationDtoResponse getById(Long compId) {
        log.debug("CompilationService->getById id={}", compId);
        Compilation result = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(compId));
        return mapper.toDto(result);
    }

    private void createCompilationEvents(Compilation compilation, List<Event> events) {
        events.forEach(event -> compilation.getEvents().add(
                CompilationEvent.builder()
                        .compilation(compilation)
                        .event(event)
                        .build()
        ));
    }
}