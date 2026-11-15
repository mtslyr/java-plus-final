package ru.practicum.ewm.event.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.clients.StatsClient;
import ru.practicum.ewm.stats.dto.StatHitResponseElement;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventUtil {

    private final StatsClient statsClient;

    public Long getViews(Long eventId) {
        try {
            List<StatHitResponseElement> stats = statsClient.getStats(
                    LocalDateTime.of(2020, 1, 1, 0, 0),
                    LocalDateTime.now(),
                    List.of("/events/" + eventId),
                    true
            );
            return stats.isEmpty() ? 0 : stats.getFirst().getHits();
        } catch (Exception e) {
            log.warn("Failed to get views for event {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }
}
