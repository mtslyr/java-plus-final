package ru.practicum.ewm.stats.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.model.RecommendedEvent;
import ru.practicum.ewm.stats.model.UserActionWeight;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface UserActionWeightRepository extends JpaRepository<UserActionWeight, UserActionWeight.Key> {

    List<UserActionWeight> findByUserId(int userId);

    List<UserActionWeight> findByUserIdOrderByLastInteractionDesc(int userId, Pageable pageable);

    @Query("""
            SELECT new ru.practicum.ewm.stats.model.RecommendedEvent(w.eventId, SUM(w.weight))
            FROM UserActionWeight w
            WHERE w.eventId IN :eventIds
            GROUP BY w.eventId
            """)
    List<RecommendedEvent> sumWeightsByEventIds(@Param("eventIds") Collection<Integer> eventIds);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO user_action_weights (user_id, event_id, weight, last_interaction)
            VALUES (:userId, :eventId, :weight, :timestamp)
            ON CONFLICT (user_id, event_id) DO UPDATE SET
                weight = GREATEST(user_action_weights.weight, EXCLUDED.weight),
                last_interaction = GREATEST(user_action_weights.last_interaction, EXCLUDED.last_interaction)
            """, nativeQuery = true)
    void upsertMaxWeight(@Param("userId") int userId,
                         @Param("eventId") int eventId,
                         @Param("weight") double weight,
                         @Param("timestamp") Instant timestamp);
}
