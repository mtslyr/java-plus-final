package ru.practicum.ewm.stats.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyzerStarter implements ApplicationRunner {

    private final UserActionLoop userActionLoop;
    private final EventSimilarityLoop eventSimilarityLoop;

    @Override
    public void run(ApplicationArguments args) {
        new Thread(userActionLoop, "user-actions-loop").start();
        new Thread(eventSimilarityLoop, "event-similarity-loop").start();
    }
}
