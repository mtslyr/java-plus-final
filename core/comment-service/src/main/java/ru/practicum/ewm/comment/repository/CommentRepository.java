package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.comment.model.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEventIdOrderByCreatedDesc(Long eventId, Pageable pageable);

    Page<Comment> findByAuthorIdOrderByCreatedDesc(Long authorId, Pageable pageable);

    @Query("""
            select c.eventId as eventId, count(c) as cnt
            from Comment c
            where c.eventId in :eventIds
            group by c.eventId
            """)
    List<CommentCountView> countByEventIdIn(@Param("eventIds") Collection<Long> eventIds);

    @Modifying
    @Query("delete from Comment c where c.authorId = :authorId")
    void deleteByAuthorId(@Param("authorId") Long authorId);

    @Modifying
    @Query("delete from Comment c where c.eventId in :eventIds")
    void deleteByEventIdIn(@Param("eventIds") Collection<Long> eventIds);

    interface CommentCountView {
        Long getEventId();

        Long getCnt();
    }
}
