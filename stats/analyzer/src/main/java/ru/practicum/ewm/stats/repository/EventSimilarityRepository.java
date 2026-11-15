package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.model.EventSimilarity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarity.Key> {

    @Query("""
            SELECT s FROM EventSimilarity s
            WHERE s.eventA IN :eventIds OR s.eventB IN :eventIds
            """)
    List<EventSimilarity> findAllWithAnyOf(@Param("eventIds") Collection<Integer> eventIds);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO event_similarity (event_a, event_b, score, updated_at)
            VALUES (:eventA, :eventB, :score, :timestamp)
            ON CONFLICT (event_a, event_b) DO UPDATE SET
                score = EXCLUDED.score,
                updated_at = EXCLUDED.updated_at
            WHERE event_similarity.updated_at <= EXCLUDED.updated_at
            """, nativeQuery = true)
    void upsert(@Param("eventA") int eventA,
                @Param("eventB") int eventB,
                @Param("score") double score,
                @Param("timestamp") Instant timestamp);
}
