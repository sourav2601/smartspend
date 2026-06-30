package com.smartspend.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fixed set of categories the ML microservice predicts into.
 * Kept as a table (not a hardcoded enum) so categories can be extended
 * later without a code change - worth mentioning in a viva/interview
 * as a forward-compatibility decision.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // e.g. icon name used by the frontend, like "food" or "travel"
    private String icon;

    public Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}
