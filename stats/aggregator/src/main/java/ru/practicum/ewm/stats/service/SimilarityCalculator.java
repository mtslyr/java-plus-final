package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

public interface SimilarityCalculator {
    List<EventSimilarityAvro> calculate(UserActionAvro userActionAvro);
}
