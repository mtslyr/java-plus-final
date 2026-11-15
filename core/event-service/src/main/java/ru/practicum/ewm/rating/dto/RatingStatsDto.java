package ru.practicum.ewm.rating.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RatingStatsDto {

    public static final RatingStatsDto EMPTY = new RatingStatsDto(0L, 0L);

    private final Long likes;
    private final Long dislikes;

    public Long getRating() {
        return likes - dislikes;
    }
}
