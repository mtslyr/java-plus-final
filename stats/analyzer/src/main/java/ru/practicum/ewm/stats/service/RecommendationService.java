package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.stats.model.RecommendedEvent;

import java.util.Collection;
import java.util.List;

public interface RecommendationService {

    List<RecommendedEvent> getRecommendationsForUser(int userId, int maxResults);

    List<RecommendedEvent> getSimilarEvents(int eventId, int userId, int maxResults);

    List<RecommendedEvent> getInteractionsCount(Collection<Integer> eventIds);
}
