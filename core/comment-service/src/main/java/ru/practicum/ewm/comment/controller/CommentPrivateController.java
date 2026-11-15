package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommentPrivateController {

    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable Long userId,
                             @PathVariable Long eventId,
                             @Valid @RequestBody NewCommentDto dto) {
        return commentService.create(userId, eventId, dto);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    public CommentDto update(@PathVariable Long userId,
                             @PathVariable Long commentId,
                             @Valid @RequestBody UpdateCommentDto dto) {
        return commentService.update(userId, commentId, dto);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserComment(@PathVariable Long userId,
                                  @PathVariable Long commentId) {
        commentService.deleteUserComment(userId, commentId);
    }

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> getByUser(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "0") Integer from,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return commentService.getByUser(userId, from, size);
    }
}
