package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.stats.dto.StatHitRequest;
import ru.practicum.ewm.stats.dto.StatHitResponseElement;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(StatHitRequest request);

    List<StatHitResponseElement> getStats(LocalDateTime start,
                                          LocalDateTime end,
                                          List<String> uris,
                                          Boolean unique);
}
