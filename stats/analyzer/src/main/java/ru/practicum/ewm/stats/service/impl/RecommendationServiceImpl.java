package ru.practicum.ewm.stats.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.model.EventSimilarity;
import ru.practicum.ewm.stats.model.RecommendedEvent;
import ru.practicum.ewm.stats.model.UserActionWeight;
import ru.practicum.ewm.stats.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.repository.UserActionWeightRepository;
import ru.practicum.ewm.stats.service.RecommendationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private static final Comparator<RecommendedEvent> BY_SCORE_DESC =
            Comparator.comparingDouble(RecommendedEvent::score).reversed();

    private final UserActionWeightRepository weightRepository;
    private final EventSimilarityRepository similarityRepository;

    @Override
    public List<RecommendedEvent> getRecommendationsForUser(int userId, int maxResults) {
        if (maxResults <= 0) {
            return List.of();
        }

        List<Integer> recentEvents = weightRepository
                .findByUserIdOrderByLastInteractionDesc(userId, PageRequest.of(0, maxResults)).stream()
                .map(UserActionWeight::getEventId)
                .toList();
        if (recentEvents.isEmpty()) {
            return List.of();
        }

        Map<Integer, Double> userWeights = userWeights(userId);
        Map<Integer, Double> candidates = new HashMap<>();
        for (EventSimilarity similarity : similarityRepository.findAllWithAnyOf(recentEvents)) {
            int newEvent = userWeights.containsKey(similarity.getEventA())
                    ? similarity.getEventB()
                    : similarity.getEventA();
            if (!userWeights.containsKey(newEvent)) {
                candidates.merge(newEvent, similarity.getScore(), Math::max);
            }
        }

        Set<Integer> newEvents = candidates.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (newEvents.isEmpty()) {
            return List.of();
        }

        Map<Integer, List<RecommendedEvent>> neighbours = new HashMap<>();
        for (EventSimilarity similarity : similarityRepository.findAllWithAnyOf(newEvents)) {
            int newEvent = newEvents.contains(similarity.getEventA())
                    ? similarity.getEventA()
                    : similarity.getEventB();
            int known = similarity.otherThan(newEvent);
            if (userWeights.containsKey(known)) {
                neighbours.computeIfAbsent(newEvent, e -> new ArrayList<>())
                        .add(new RecommendedEvent(known, similarity.getScore()));
            }
        }

        return neighbours.entrySet().stream()
                .map(entry -> new RecommendedEvent(entry.getKey(),
                        predictScore(entry.getValue(), userWeights, maxResults)))
                .sorted(BY_SCORE_DESC)
                .toList();
    }

    @Override
    public List<RecommendedEvent> getSimilarEvents(int eventId, int userId, int maxResults) {
        if (maxResults <= 0) {
            return List.of();
        }

        Set<Integer> userEvents = userWeights(userId).keySet();

        return similarityRepository.findAllWithAnyOf(List.of(eventId)).stream()
                .map(similarity -> new RecommendedEvent(similarity.otherThan(eventId), similarity.getScore()))
                .filter(event -> !userEvents.contains(event.eventId()))
                .sorted(BY_SCORE_DESC)
                .limit(maxResults)
                .toList();
    }

    @Override
    public List<RecommendedEvent> getInteractionsCount(Collection<Integer> eventIds) {
        if (eventIds.isEmpty()) {
            return List.of();
        }
        return weightRepository.sumWeightsByEventIds(Set.copyOf(eventIds));
    }

    private Map<Integer, Double> userWeights(int userId) {
        return weightRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserActionWeight::getEventId, UserActionWeight::getWeight));
    }

    private double predictScore(List<RecommendedEvent> neighbours, Map<Integer, Double> userWeights, int k) {
        List<RecommendedEvent> nearest = neighbours.stream()
                .sorted(BY_SCORE_DESC)
                .limit(k)
                .toList();

        double weightedSum = 0;
        double similaritySum = 0;
        for (RecommendedEvent neighbour : nearest) {
            weightedSum += userWeights.get(neighbour.eventId()) * neighbour.score();
            similaritySum += neighbour.score();
        }
        return weightedSum / similaritySum;
    }
}
