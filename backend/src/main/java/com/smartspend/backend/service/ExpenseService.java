package com.smartspend.backend.service;

import com.smartspend.backend.dto.ExpenseDtos;
import com.smartspend.backend.dto.MlServiceDtos;
import com.smartspend.backend.entity.Category;
import com.smartspend.backend.entity.Expense;
import com.smartspend.backend.entity.User;
import com.smartspend.backend.exception.ResourceNotFoundException;
import com.smartspend.backend.repository.CategoryRepository;
import com.smartspend.backend.repository.ExpenseRepository;
import com.smartspend.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MlClassifierClient mlClassifierClient;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            MlClassifierClient mlClassifierClient
    ) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.mlClassifierClient = mlClassifierClient;
    }

    @Transactional
    public ExpenseDtos.ExpenseResponse create(Long userId, ExpenseDtos.CreateExpenseRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Expense expense = new Expense();
        expense.setOwner(owner);
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setDate(request.date());

        if (request.categoryId() != null) {
            // User manually chose a category at entry time - skip the ML call.
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            expense.setCategory(category);
            expense.setCategoryIsAiPredicted(false);
        } else {
            // No category given - ask the ML microservice to predict one.
            MlServiceDtos.CategorizeResponse prediction = mlClassifierClient.categorize(request.description());
            Category category = categoryRepository.findByNameIgnoreCase(prediction.category())
                    .orElseGet(() -> categoryRepository.findByNameIgnoreCase("Other")
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Default 'Other' category missing - check data seeding")));
            expense.setCategory(category);
            expense.setCategoryIsAiPredicted(true);
            expense.setAiConfidence(prediction.confidence());
        }

        expenseRepository.save(expense);
        return ExpenseDtos.ExpenseResponse.from(expense);
    }

    @Transactional
    public ExpenseDtos.ExpenseResponse update(Long userId, Long expenseId, ExpenseDtos.UpdateExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getOwner().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (request.description() != null) expense.setDescription(request.description());
        if (request.amount() != null) expense.setAmount(request.amount());
        if (request.date() != null) expense.setDate(request.date());

        if (request.categoryId() != null) {
            // User is correcting the AI's category - this is the feedback
            // signal that, over time, could be used to retrain/improve
            // the ML model (e.g. exporting corrected (description, category)
            // pairs as new training data).
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            expense.setCategory(category);
            expense.setCategoryIsAiPredicted(false);
            expense.setAiConfidence(null);
        }

        return ExpenseDtos.ExpenseResponse.from(expense);
    }

    @Transactional
    public void delete(Long userId, Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getOwner().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    public List<ExpenseDtos.ExpenseResponse> listAll(Long userId) {
        return expenseRepository.findByOwnerIdOrderByDateDesc(userId)
                .stream()
                .map(ExpenseDtos.ExpenseResponse::from)
                .toList();
    }

    public List<ExpenseDtos.ExpenseResponse> listInRange(Long userId, LocalDate start, LocalDate end) {
        return expenseRepository.findByOwnerIdAndDateBetweenOrderByDateDesc(userId, start, end)
                .stream()
                .map(ExpenseDtos.ExpenseResponse::from)
                .toList();
    }

    /**
     * Powers the dashboard's pie/bar charts: total + percentage + count
     * per category within a date range.
     */
    public List<ExpenseDtos.CategorySummary> categorySummary(Long userId, LocalDate start, LocalDate end) {
        List<Object[]> rows = expenseRepository.aggregateByCategory(userId, start, end);

        double grandTotal = rows.stream()
                .mapToDouble(r -> ((Number) r[1]).doubleValue())
                .sum();

        return rows.stream()
                .map(r -> {
                    String category = r[0] == null ? "Uncategorized" : (String) r[0];
                    double total = ((Number) r[1]).doubleValue();
                    long count = ((Number) r[2]).longValue();
                    double percentage = grandTotal == 0 ? 0 : (total / grandTotal) * 100.0;
                    return new ExpenseDtos.CategorySummary(category, total, percentage, count);
                })
                .toList();
    }
}
