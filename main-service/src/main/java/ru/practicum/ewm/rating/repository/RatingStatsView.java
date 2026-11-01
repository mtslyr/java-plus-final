package ru.practicum.ewm.rating.repository;

public interface RatingStatsView {

    Long getEventId();

    Long getLikes();

    Long getDislikes();
}
