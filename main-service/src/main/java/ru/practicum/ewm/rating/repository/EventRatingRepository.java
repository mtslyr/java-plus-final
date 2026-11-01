package ru.practicum.ewm.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.rating.model.EventRating;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRatingRepository extends JpaRepository<EventRating, Long> {

    Optional<EventRating> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("""
            select r.event.id as eventId,
                   sum(case when r.type = ru.practicum.ewm.rating.model.RatingType.LIKE then 1 else 0 end) as likes,
                   sum(case when r.type = ru.practicum.ewm.rating.model.RatingType.DISLIKE then 1 else 0 end) as dislikes
            from EventRating r
            where r.event.id in :eventIds
            group by r.event.id
            """)
    List<RatingStatsView> findStatsByEventIds(@Param("eventIds") Collection<Long> eventIds);
}
