package com.smartspend.backend.controller;

import com.smartspend.backend.dto.ExpenseDtos;
import com.smartspend.backend.security.CurrentUser;
import com.smartspend.backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseDtos.ExpenseResponse> create(@Valid @RequestBody ExpenseDtos.CreateExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(CurrentUser.id(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDtos.ExpenseResponse> update(
            @PathVariable Long id,
            @RequestBody ExpenseDtos.UpdateExpenseRequest request
    ) {
        return ResponseEntity.ok(expenseService.update(CurrentUser.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(CurrentUser.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDtos.ExpenseResponse>> listAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        if (start != null && end != null) {
            return ResponseEntity.ok(expenseService.listInRange(CurrentUser.id(), start, end));
        }
        return ResponseEntity.ok(expenseService.listAll(CurrentUser.id()));
    }

    /** Powers the dashboard pie/bar charts. */
    @GetMapping("/summary")
    public ResponseEntity<List<ExpenseDtos.CategorySummary>> categorySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(expenseService.categorySummary(CurrentUser.id(), start, end));
    }
}
