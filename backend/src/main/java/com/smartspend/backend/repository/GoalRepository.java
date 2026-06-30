package com.smartspend.backend.repository;

import com.smartspend.backend.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Goal> findByIdAndOwnerId(Long id, Long ownerId);
}
