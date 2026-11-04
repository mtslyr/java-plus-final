package ru.practicum.ewm.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.comment.repository.CommentRepository.CommentCountView;
import ru.practicum.ewm.interaction.api.CommentApi;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalCommentController implements CommentApi {

    private final CommentRepository commentRepository;

    @Override
    public Map<Long, Long> getCommentCounts(Collection<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return commentRepository.countByEventIdIn(eventIds).stream()
                .collect(Collectors.toMap(CommentCountView::getEventId, CommentCountView::getCnt));
    }

    @Override
    @Transactional
    public void deleteUserData(Long userId, Collection<Long> eventIds) {
        commentRepository.deleteByAuthorId(userId);
        if (eventIds != null && !eventIds.isEmpty()) {
            commentRepository.deleteByEventIdIn(eventIds);
        }
    }
}
