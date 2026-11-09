package ru.practicum.ewm.compilations.models;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.event.model.Event;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "compilation_events")
public class CompilationEvent {
    @Id
    @Column(name = "cmp_event_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compilation_id", nullable = false)
    private Compilation compilation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}