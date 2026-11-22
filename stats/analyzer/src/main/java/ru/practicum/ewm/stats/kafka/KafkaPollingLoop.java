package ru.practicum.ewm.stats.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class KafkaPollingLoop<K, V> implements Runnable {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 15;

    private final KafkaConsumer<K, V> consumer;
    private final String topic;

    private final CountDownLatch stopped = new CountDownLatch(1);
    private volatile boolean running;

    protected KafkaPollingLoop(KafkaConsumer<K, V> consumer, String topic) {
        this.consumer = consumer;
        this.topic = topic;
    }

    protected abstract void handle(V value);

    @Override
    public void run() {
        running = true;
        try {
            consumer.subscribe(List.of(topic));
            log.info("Подписка на топик {}", topic);

            while (true) {
                ConsumerRecords<K, V> records = consumer.poll(POLL_TIMEOUT);
                if (records.isEmpty()) {
                    continue;
                }

                for (ConsumerRecord<K, V> record : records) {
                    handle(record.value());
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.info("Чтение топика {} останавливается", topic);
        } catch (Exception e) {
            log.error("Чтение топика {} остановлено из-за ошибки", topic, e);
        } finally {
            try {
                consumer.close();
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
            log.warn("Чтение топика {} не завершилось за {} с", topic, SHUTDOWN_TIMEOUT_SECONDS);
        }
    }
}
