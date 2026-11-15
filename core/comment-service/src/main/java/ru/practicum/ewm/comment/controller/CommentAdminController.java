package ru.practicum.ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.service.CommentService;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(@PathVariable Long commentId) {
        commentService.deleteByAdmin(commentId);
    }
}
