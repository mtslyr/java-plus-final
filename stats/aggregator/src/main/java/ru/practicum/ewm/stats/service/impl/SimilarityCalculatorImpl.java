package ru.practicum.ewm.stats.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.configuration.WeightsConfig;
import ru.practicum.ewm.stats.service.SimilarityCalculator;

import java.time.Instant;
import java.util.*;

import static java.lang.Math.sqrt;

@Component
@RequiredArgsConstructor
public class SimilarityCalculatorImpl implements SimilarityCalculator {
    private final WeightsConfig weightsConfig;

    private final Map<Integer, Map<Integer, Double>> weights = new HashMap<>();

    private final Map<Integer, Double> eventWeightSums = new HashMap<>();

    private final Map<Integer, Map<Integer, Double>> minWeightsSums = new HashMap<>();

    @Override
    public List<EventSimilarityAvro> calculate(UserActionAvro userActionAvro) {
        int user = userActionAvro.getUserId();
        int event = userActionAvro.getEventId();
        double actionWeight = weightsConfig.weightOf(userActionAvro.getActionType());

        Map<Integer, Double> eventUsers = weights.computeIfAbsent(event, e -> new HashMap<>());
        double old = eventUsers.getOrDefault(user, 0.0);
        double newWeight = Math.max(old, actionWeight);

        if (old == newWeight) {
            return Collections.emptyList();
        }

        eventUsers.put(user, newWeight);
        eventWeightSums.merge(event, newWeight - old, Double::sum);

        for (Map.Entry<Integer, Map<Integer, Double>> entry : weights.entrySet()) {
            int otherEvent = entry.getKey();
            Double otherWeight = entry.getValue().get(user);

            if (otherEvent == event || otherWeight == null) {
                continue;
            }
            double delta = Math.min(newWeight, otherWeight) - Math.min(old, otherWeight);
            if (delta != 0) {
                addMinSum(event, otherEvent, delta);
            }
        }

        List<EventSimilarityAvro> result = new ArrayList<>();
        for (int otherEvent : weights.keySet()) {
            if (otherEvent == event) {
                continue;
            }
            double minSum = getMinSum(event, otherEvent);
            if (minSum > 0) {
                double score = score(event, otherEvent, minSum);
                result.add(toAvro(event, otherEvent, score, userActionAvro.getTimestamp()));
            }
        }

        return result;
    }

    private double score(int eventA, int eventB, double minSum) {
        return minSum / (sqrt(eventWeightSums.get(eventA)) * sqrt(eventWeightSums.get(eventB)));
    }

    private void addMinSum(int a, int b, double delta) {
        minWeightsSums.computeIfAbsent(Math.min(a, b), k -> new HashMap<>())
                .merge(Math.max(a, b), delta, Double::sum);
    }

    private double getMinSum(int eventA, int eventB) {
        return minWeightsSums.getOrDefault(Math.min(eventA, eventB), Map.of())
                .getOrDefault(Math.max(eventA, eventB), 0.0);
    }

    private EventSimilarityAvro toAvro(int eventA, int eventB, double score, Instant timestamp) {
        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(eventA, eventB))
                .setEventB(Math.max(eventA, eventB))
                .setScore(score)
                .setTimestamp(timestamp)
                .build();
    }
}
