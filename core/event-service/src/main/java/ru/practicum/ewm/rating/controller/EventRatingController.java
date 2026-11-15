package ru.practicum.ewm.rating.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.rating.model.RatingType;
import ru.practicum.ewm.rating.service.RatingService;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}")
@RequiredArgsConstructor
public class EventRatingController {

    private final RatingService ratingService;

    @PutMapping("/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.setRating(userId, eventId, RatingType.LIKE);
    }

    @PutMapping("/dislike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dislike(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.setRating(userId, eventId, RatingType.DISLIKE);
    }

    @DeleteMapping("/rating")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.deleteRating(userId, eventId);
    }
}
