package com.smartspend.backend.repository;

import com.smartspend.backend.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByOwnerIdOrderByDateDesc(Long ownerId);

    List<Expense> findByOwnerIdAndDateBetweenOrderByDateDesc(
            Long ownerId, LocalDate start, LocalDate end);

    /**
     * Aggregation used by the dashboard's pie/bar charts: total spent
     * per category for a given user within a date range.
     * Returns rows of [categoryName, totalAmount, count].
     */
    @Query("""
            SELECT c.name, SUM(e.amount), COUNT(e)
            FROM Expense e
            LEFT JOIN e.category c
            WHERE e.owner.id = :ownerId
            AND e.date BETWEEN :start AND :end
            GROUP BY c.name
            """)
    List<Object[]> aggregateByCategory(
            @Param("ownerId") Long ownerId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    /**
     * Raw expenses since a given date, used by the savings/goal engine
     * to compute average monthly spend per category. Deliberately kept
     * as a flat fetch + in-memory aggregation in the service layer
     * (see GoalService) rather than a subquery-in-FROM JPQL query -
     * Hibernate's support for derived tables varies across versions,
     * and this calculation only ever runs for one user at a time, so
     * the dataset is small enough that in-memory grouping is simpler
     * and more portable than fighting the query.
     */
    List<Expense> findByOwnerIdAndDateGreaterThanEqual(Long ownerId, LocalDate since);
}
