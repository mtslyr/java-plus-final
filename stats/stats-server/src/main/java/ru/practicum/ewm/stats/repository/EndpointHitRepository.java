package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stats.dto.StatHitResponseElement;
import ru.practicum.ewm.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    @Query("""
            select new ru.practicum.ewm.stats.dto.StatHitResponseElement(h.app, h.uri, count(h.ip))
            from EndpointHit h
            where h.timestamp between :start and :end
            group by h.app, h.uri
            order by count(h.ip) desc
            """)
    List<StatHitResponseElement> findStats(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("""
            select new ru.practicum.ewm.stats.dto.StatHitResponseElement(h.app, h.uri, count(h.ip))
            from EndpointHit h
            where h.timestamp between :start and :end
              and h.uri in :uris
            group by h.app, h.uri
            order by count(h.ip) desc
            """)
    List<StatHitResponseElement> findStats(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("uris") List<String> uris);

    @Query("""
            select new ru.practicum.ewm.stats.dto.StatHitResponseElement(h.app, h.uri, count(distinct h.ip))
            from EndpointHit h
            where h.timestamp between :start and :end
            group by h.app, h.uri
            order by count(distinct h.ip) desc
            """)
    List<StatHitResponseElement> findUniqueStats(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    @Query("""
            select new ru.practicum.ewm.stats.dto.StatHitResponseElement(h.app, h.uri, count(distinct h.ip))
            from EndpointHit h
            where h.timestamp between :start and :end
              and h.uri in :uris
            group by h.app, h.uri
            order by count(distinct h.ip) desc
            """)
    List<StatHitResponseElement> findUniqueStats(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("uris") List<String> uris);
}
