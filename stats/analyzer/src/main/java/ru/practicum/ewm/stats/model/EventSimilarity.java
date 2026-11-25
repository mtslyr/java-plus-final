package ru.practicum.ewm.stats.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "event_similarity")
@IdClass(EventSimilarity.Key.class)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class EventSimilarity {

    @Id
    @Column(name = "event_a")
    private Integer eventA;

    @Id
    @Column(name = "event_b")
    private Integer eventB;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public int otherThan(int eventId) {
        return eventA == eventId ? eventB : eventA;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {
        private Integer eventA;
        private Integer eventB;
    }
}
