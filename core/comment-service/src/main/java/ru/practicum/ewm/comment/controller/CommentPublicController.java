package ru.practicum.ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommentPublicController {

    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getByEvent(@PathVariable Long eventId,
                                       @RequestParam(defaultValue = "0") Integer from,
                                       @RequestParam(defaultValue = "10") Integer size) {
        return commentService.getByEvent(eventId, from, size);
    }

    @GetMapping("/events/{eventId}/comments/{commentId}")
    public CommentDto getOne(@PathVariable Long eventId,
                             @PathVariable Long commentId) {
        return commentService.getOne(eventId, commentId);
    }
}
