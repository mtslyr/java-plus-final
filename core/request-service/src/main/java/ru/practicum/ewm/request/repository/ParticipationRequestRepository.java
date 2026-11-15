package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByIdIn(List<Long> requestIds);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("""
            select r.eventId as eventId, count(r) as cnt
            from ParticipationRequest r
            where r.eventId in :eventIds
              and r.status = :status
            group by r.eventId
            """)
    List<RequestCountView> countByEventIdInAndStatus(@Param("eventIds") Collection<Long> eventIds,
                                                     @Param("status") RequestStatus status);

    @Modifying
    @Query("delete from ParticipationRequest r where r.requesterId = :requesterId")
    void deleteByRequesterId(@Param("requesterId") Long requesterId);

    @Modifying
    @Query("delete from ParticipationRequest r where r.eventId in :eventIds")
    void deleteByEventIdIn(@Param("eventIds") Collection<Long> eventIds);

    interface RequestCountView {
        Long getEventId();

        Long getCnt();
    }
}
