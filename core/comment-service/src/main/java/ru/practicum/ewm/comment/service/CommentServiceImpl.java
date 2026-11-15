package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.exception.CommentNotFoundException;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.EventNotFoundException;
import ru.practicum.ewm.common.exception.UserNotFoundException;
import ru.practicum.ewm.interaction.api.EventApi;
import ru.practicum.ewm.interaction.api.UserApi;
import ru.practicum.ewm.interaction.dto.EventState;
import ru.practicum.ewm.interaction.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserApi userApi;
    private final EventApi eventApi;
    private final CommentMapper mapper;

    @Override
    @Transactional
    public CommentDto create(Long userId, Long eventId, NewCommentDto dto) {
        checkUserExists(userId);
        if (eventApi.getById(eventId).getState() != EventState.PUBLISHED) {
            throw new EventNotFoundException(eventId);
        }

        Comment comment = mapper.toComment(dto, eventId, userId);
        comment = commentRepository.save(comment);
        log.debug("Comment created: {} by user {} on event {}", comment.getId(), userId, eventId);
        return toDto(comment);
    }

    @Override
    @Transactional
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto dto) {
        checkUserExists(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("Only the author can edit the comment");
        }

        comment.setText(dto.getText());
        comment.setUpdated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        log.debug("Comment updated: {}", commentId);
        return toDto(comment);
    }

    @Override
    @Transactional
    public void deleteUserComment(Long userId, Long commentId) {
        checkUserExists(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConflictException("Only the author can delete the comment");
        }

        commentRepository.delete(comment);
        log.debug("Comment deleted by author: {}", commentId);
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        commentRepository.delete(comment);
        log.debug("Comment deleted by admin: {}", commentId);
    }

    @Override
    public List<CommentDto> getByUser(Long userId, Integer from, Integer size) {
        checkUserExists(userId);
        PageRequest page = PageRequest.of(from / size, size);
        return toDtos(commentRepository.findByAuthorIdOrderByCreatedDesc(userId, page).getContent());
    }

    @Override
    public List<CommentDto> getByEvent(Long eventId, Integer from, Integer size) {
        eventApi.getById(eventId);
        PageRequest page = PageRequest.of(from / size, size);
        return toDtos(commentRepository.findByEventIdOrderByCreatedDesc(eventId, page).getContent());
    }

    @Override
    public CommentDto getOne(Long eventId, Long commentId) {
        eventApi.getById(eventId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getEventId().equals(eventId)) {
            throw new CommentNotFoundException(commentId);
        }
        return toDto(comment);
    }

    private void checkUserExists(Long userId) {
        if (!userApi.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private CommentDto toDto(Comment comment) {
        return toDtos(List.of(comment)).getFirst();
    }

    private List<CommentDto> toDtos(List<Comment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }
        List<Long> authorIds = comments.stream().map(Comment::getAuthorId).distinct().toList();
        Map<Long, String> authorNames = userApi.getShortByIds(authorIds).stream()
                .collect(Collectors.toMap(UserShortDto::getId, UserShortDto::getName));
        return comments.stream()
                .map(c -> mapper.toCommentDto(c, authorNames.get(c.getAuthorId())))
                .toList();
    }
}
