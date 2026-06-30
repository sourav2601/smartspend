package com.smartspend.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // e.g. "Swiggy order", "Uber to airport"
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate date;

    // True if the ML microservice assigned this category; false if the
    // user manually picked/overrode it. This flag feeds the feedback
    // loop that improves the classifier over time.
    @Column(nullable = false)
    private boolean categoryIsAiPredicted = true;

    // Model's confidence score, 0-1. Null if manually set by user.
    private Double aiConfidence;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
