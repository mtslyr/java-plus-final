package ru.practicum.ewm.compilations.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @Column(name = "compilation_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pinned")
    private Boolean pinned;

    @Column(name = "title", length = 50)
    private String title;

    @OneToMany(mappedBy = "compilation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CompilationEvent> events;
}