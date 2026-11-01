package ru.practicum.ewm.locations.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.locations.models.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query("SELECT l FROM Location l ORDER BY l.id")
    List<Location> findAllWithOffset(Pageable pageable);

    @Query(value = """
            SELECT e.event_id FROM events e
            JOIN locations l ON l.event_id = e.event_id
            WHERE distance(l.lat, l.lon, :lat, :lon) <= :radius
            ORDER BY e.event_date
            """, nativeQuery = true)
    List<Long> findEventIdsWithinRadius(@Param("lat") Float lat,
                                        @Param("lon") Float lon,
                                        @Param("radius") Float radius);
}