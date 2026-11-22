package ru.practicum.ewm.stats.kafka;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.configuration.KafkaTopicProperties;
import ru.practicum.ewm.stats.service.SimilarityCalculator;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorRunner implements ApplicationRunner {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 15;

    private final KafkaConsumer<Long, UserActionAvro> consumer;
    private final EventSimilarityProducer producer;
    private final SimilarityCalculator calculator;
    private final KafkaTopicProperties topics;

    private final CountDownLatch stopped = new CountDownLatch(1);
    private volatile boolean running;

    @Override
    public void run(ApplicationArguments args) {
        running = true;
        try {
            consumer.subscribe(List.of(topics.userActions()));
            log.info("Aggregator подписан на топик {}", topics.userActions());

            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(POLL_TIMEOUT);
                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    calculator.calculate(record.value()).forEach(producer::send);
                }

                producer.flush();
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("Aggregator останавливается");
        } finally {
            try {
                consumer.close();
                producer.close();
            } finally {
                stopped.countDown();
            }
        }
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        if (!running) {
            return;
        }
        consumer.wakeup();
        if (!stopped.await(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            log.warn("Цикл обработки не завершился за {} с", SHUTDOWN_TIMEOUT_SECONDS);
        }
    }
}
