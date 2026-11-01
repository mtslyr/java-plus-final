package ru.practicum.ewm.compilations.controllers;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilations.dto.CompilationDtoResponse;
import ru.practicum.ewm.compilations.services.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated
public class PublicCompilationController {
    public static final String PARAM_PINNED = "pinned";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_FROM = "from";

    private final CompilationService compilationsService;

    @GetMapping
    public ResponseEntity<List<CompilationDtoResponse>> get(
            @RequestParam(name = PARAM_PINNED, required = false) Boolean pinned,
            @RequestParam(name = PARAM_FROM, required = false, defaultValue = "0")
            @PositiveOrZero Integer from,
            @RequestParam(name = PARAM_SIZE, required = false, defaultValue = "10")
            @Positive Integer size
    ) {

        Pageable pageable = PageRequest.of(from / size, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationsService.getCompilations(pinned, pageable));
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDtoResponse> getById(
            @PathVariable(name = "compId")
            Long compId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationsService.getById(compId));
    }
}
