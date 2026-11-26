package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    default Page<Task> findOwnedTasks(
            Long userId,
            TaskStatus status,
            TaskPriority priority,
            String title,
            LocalDate dueFrom,
            LocalDate dueTo,
            Pageable pageable
    ) {
        Specification<Task> specification = (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.<TaskStatus>get("status"), status));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.<TaskPriority>get("priority"), priority));
            }
            if (title != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.<String>get("title")),
                        "%" + title.toLowerCase(Locale.ROOT) + "%"
                ));
            }
            if (dueFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<LocalDate>get("dueDate"), dueFrom
                ));
            }
            if (dueTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<LocalDate>get("dueDate"), dueTo
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        return findAll(specification, pageable);
    }

    Optional<Task> findByIdAndUserId(Long taskId, Long userId);

    boolean existsByIdAndUserId(Long taskId, Long userId);
}
