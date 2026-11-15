package ru.practicum.ewm.stats.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.configuration.WeightsConfig;
import ru.practicum.ewm.stats.service.SimilarityCalculator;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static ru.practicum.ewm.stats.avro.ActionTypeAvro.*;

@DisplayName("Тесты SimilarityCalculator")
public class SimilarityCalculatorTest {

    private static final Instant TIMESTAMP = Instant.ofEpochMilli(1_700_000_000_000L);

    private final SimilarityCalculator calculator =
            new SimilarityCalculatorImpl(new WeightsConfig(0.4, 0.8, 1.0));

    private static UserActionAvro action(int userId, int eventId, ActionTypeAvro type) {
        return UserActionAvro.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(type)
                .setTimestamp(TIMESTAMP)
                .build();
    }

    private static void assertSingleSimilarity(List<EventSimilarityAvro> result,
                                               int eventA, int eventB, double score) {
        assertThat(result).hasSize(1);
        EventSimilarityAvro similarity = result.get(0);
        assertThat(similarity.getEventA()).isEqualTo(eventA);
        assertThat(similarity.getEventB()).isEqualTo(eventB);
        assertThat(similarity.getScore()).isCloseTo(score, within(0.001));
    }

    @Test
    @DisplayName("Расчёт сходства мероприятий")
    void shouldCalculateSimilarity() {
        assertThat(calculator.calculate(action(1, 10, LIKE))).isEmpty();

        assertSingleSimilarity(calculator.calculate(action(1, 20, VIEW)), 10, 20, 0.632);

        assertSingleSimilarity(calculator.calculate(action(2, 20, LIKE)), 10, 20, 0.338);

        assertSingleSimilarity(calculator.calculate(action(1, 20, LIKE)), 10, 20, 0.707);

        assertThat(calculator.calculate(action(1, 10, VIEW))).isEmpty();
    }

    @Test
    @DisplayName("Пара всегда упорядочена: eventA < eventB")
    void shouldOrderPair() {
        calculator.calculate(action(1, 20, LIKE));

        assertSingleSimilarity(calculator.calculate(action(1, 10, LIKE)), 10, 20, 1.0);
    }

    @Test
    @DisplayName("Новое мероприятие дает пары со всеми мероприятиями пользователя")
    void shouldReturnPairsWithAllUserEvents() {
        calculator.calculate(action(1, 10, LIKE));
        calculator.calculate(action(1, 20, LIKE));

        List<EventSimilarityAvro> result = calculator.calculate(action(1, 30, LIKE));

        assertThat(result)
                .extracting(EventSimilarityAvro::getEventA, EventSimilarityAvro::getEventB)
                .containsExactlyInAnyOrder(tuple(10, 30), tuple(20, 30));
        assertThat(result)
                .extracting(EventSimilarityAvro::getScore)
                .allSatisfy(score -> assertThat(score).isCloseTo(1.0, within(0.001)));
    }

    @Test
    @DisplayName("Повторное сравнение не влияет на результат")
    void shouldIgnoreRepeatedAction() {
        calculator.calculate(action(1, 10, LIKE));
        calculator.calculate(action(1, 20, LIKE));

        assertThat(calculator.calculate(action(1, 20, LIKE))).isEmpty();
    }

    @Test
    @DisplayName("Без общих пользователей результата нет")
    void shouldIgnoreWithoutCommonUsers() {
        assertThat(calculator.calculate(action(1, 10, LIKE))).isEmpty();
        assertThat(calculator.calculate(action(2, 20, LIKE))).isEmpty();
    }

    @Test
    @DisplayName("Score пересчитывается даже если S_min не изменилась")
    void shouldRecalculateScoreWithSameSmin() {
        calculator.calculate(action(1, 10, VIEW));
        assertSingleSimilarity(calculator.calculate(action(1, 20, REGISTER)), 10, 20, 0.707);

        assertSingleSimilarity(calculator.calculate(action(1, 20, LIKE)), 10, 20, 0.632);
    }

    @Test
    @DisplayName("Timestamp результата равен времени действия")
    void shouldUseActionTimestamp() {
        calculator.calculate(action(1, 10, LIKE));

        List<EventSimilarityAvro> result = calculator.calculate(action(1, 20, LIKE));

        assertThat(result).singleElement()
                .extracting(EventSimilarityAvro::getTimestamp)
                .isEqualTo(TIMESTAMP);
    }
}
