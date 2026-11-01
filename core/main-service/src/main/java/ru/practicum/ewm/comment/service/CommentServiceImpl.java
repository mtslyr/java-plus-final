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
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.exception.UserNotFoundException;
import ru.practicum.ewm.event.exception.EventNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper mapper;

    @Override
    @Transactional
    public CommentDto create(Long userId, Long eventId, NewCommentDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        Comment comment = mapper.toComment(dto, event, author);
        comment = commentRepository.save(comment);
        log.debug("Comment created: {} by user {} on event {}", comment.getId(), userId, eventId);
        return mapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto dto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Only the author can edit the comment");
        }
        comment.setText(dto.getText());
        comment.setUpdated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        log.debug("Comment updated: {}", commentId);
        return mapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteUserComment(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getAuthor().getId().equals(userId)) {
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
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        PageRequest page = PageRequest.of(from / size, size);
        return commentRepository.findByAuthorIdOrderByCreatedDesc(userId, page).stream()
                .map(mapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getByEvent(Long eventId, Integer from, Integer size) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        PageRequest page = PageRequest.of(from / size, size);
        return commentRepository.findByEventIdOrderByCreatedDesc(eventId, page).stream()
                .map(mapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getOne(Long eventId, Long commentId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!comment.getEvent().getId().equals(eventId)) {
            throw new CommentNotFoundException(commentId);
        }
        return mapper.toCommentDto(comment);
    }
}
