package ru.practicum.ewm.compilations.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilations.dto.CompilationDtoResponse;
import ru.practicum.ewm.compilations.dto.NewCompilationDto;
import ru.practicum.ewm.compilations.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilations.services.CompilationService;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDtoResponse> create(@RequestBody
                                                         @Valid NewCompilationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compilationService.create(dto));
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDtoResponse> patch(@PathVariable(name = "compId") Long compId,
                                                        @RequestBody
                                                        @Valid UpdateCompilationRequest dto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationService.patch(compId, dto));
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> delete(@PathVariable(name = "compId") Long compId) {
        compilationService.delete(compId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}