package com.smartspend.backend.dto;

import com.smartspend.backend.entity.Expense;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class ExpenseDtos {

    public record CreateExpenseRequest(
            @NotBlank String description,
            @Positive Double amount,
            @NotNull LocalDate date,
            // Optional: if provided, the user is manually setting the category
            // and the ML classifier is skipped for this expense.
            Long categoryId
    ) {}

    public record UpdateExpenseRequest(
            String description,
            Double amount,
            LocalDate date,
            // Setting this implies the user is overriding the AI's prediction -
            // the service layer flips categoryIsAiPredicted to false when this is used.
            Long categoryId
    ) {}

    public record CategoryResponse(Long id, String name, String icon) {}

    public record ExpenseResponse(
            Long id,
            String description,
            Double amount,
            LocalDate date,
            CategoryResponse category,
            boolean categoryIsAiPredicted,
            Double aiConfidence
    ) {
        public static ExpenseResponse from(Expense e) {
            CategoryResponse categoryDto = e.getCategory() == null ? null :
                    new CategoryResponse(e.getCategory().getId(), e.getCategory().getName(), e.getCategory().getIcon());

            return new ExpenseResponse(
                    e.getId(),
                    e.getDescription(),
                    e.getAmount(),
                    e.getDate(),
                    categoryDto,
                    e.isCategoryIsAiPredicted(),
                    e.getAiConfidence()
            );
        }
    }

    /** Used for dashboard pie/bar chart data. */
    public record CategorySummary(String category, Double total, Double percentage, Long count) {}
}
