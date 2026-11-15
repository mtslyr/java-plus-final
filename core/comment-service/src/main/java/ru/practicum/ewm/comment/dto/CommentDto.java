package ru.practicum.ewm.comment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;
    private String text;
    private Long authorId;
    private String authorName;
    private Long eventId;
    private LocalDateTime created;
    private LocalDateTime updated;
}
