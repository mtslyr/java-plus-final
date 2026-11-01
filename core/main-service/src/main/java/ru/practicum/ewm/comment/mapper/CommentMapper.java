package ru.practicum.ewm.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public Comment toComment(NewCommentDto dto, Event event, User author) {
        return Comment.builder()
                .text(dto.getText())
                .event(event)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .eventId(comment.getEvent().getId())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .build();
    }
}
