package com.smartspend.backend.config;

import com.smartspend.backend.entity.Category;
import com.smartspend.backend.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures a fixed starter set of categories exists in the database on
 * every startup. Idempotent: only inserts categories that don't already
 * exist by name, so re-running this on every boot is safe.
 *
 * These names MUST match the labels the Python ML microservice's
 * classifier was trained to predict - see ml-service/app/categorize.py.
 * If you add a category here, also add training examples for it there.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private static final List<Category> DEFAULT_CATEGORIES = List.of(
            new Category("Food", "utensils"),
            new Category("Travel", "car"),
            new Category("Shopping", "shopping-bag"),
            new Category("Subscriptions", "repeat"),
            new Category("Bills & Utilities", "file-text"),
            new Category("Entertainment", "film"),
            new Category("Health", "heart"),
            new Category("Education", "book"),
            new Category("Other", "more-horizontal")
    );

    @Override
    public void run(String... args) {
        for (Category defaultCategory : DEFAULT_CATEGORIES) {
            categoryRepository.findByNameIgnoreCase(defaultCategory.getName())
                    .orElseGet(() -> categoryRepository.save(defaultCategory));
        }
    }
}
