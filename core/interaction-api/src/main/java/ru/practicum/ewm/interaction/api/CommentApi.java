package ru.practicum.ewm.interaction.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Map;

public interface CommentApi {

    @GetMapping("/internal/comments/count")
    Map<Long, Long> getCommentCounts(@RequestParam Collection<Long> eventIds);

    @DeleteMapping("/internal/comments/users/{userId}")
    void deleteUserData(@PathVariable Long userId, @RequestParam(required = false) Collection<Long> eventIds);
}
