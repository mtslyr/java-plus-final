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
@Table(name = "user_action_weights")
@IdClass(UserActionWeight.Key.class)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserActionWeight {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "last_interaction", nullable = false)
    private Instant lastInteraction;

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {
        private Integer userId;
        private Integer eventId;
    }
}
