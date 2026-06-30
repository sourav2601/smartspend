package com.smartspend.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

/**
 * Represents a savings goal, e.g. "Buy an iPhone for Rs 70,000 by Sept 30".
 * `plan` stores the latest AI-generated savings plan as JSON so the
 * frontend can render it directly without recomputation, and so we keep
 * the most recent version of how the plan changed as spending behavior
 * changed (older plans aren't versioned/kept - only the latest).
 */
@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String title; // e.g. "iPhone 16"

    @Column(nullable = false)
    private Double targetAmount; // e.g. 70000

    private LocalDate targetDate; // optional deadline

    @Column(nullable = false)
    private Double currentSaved = 0.0;

    // Latest AI-generated plan, e.g.:
    // {"cuts": [{"category": "Food Delivery", "reduceBy": 1600, ...}],
    //  "newMonthlySavings": 4200, "projectedCompletionDate": "2026-09-15"}
    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> plan;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
