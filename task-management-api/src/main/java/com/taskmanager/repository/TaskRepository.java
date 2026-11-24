package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.enums.TaskPriority;
import com.taskmanager.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            select task
            from Task task
            where task.user.id = :userId
              and (:status is null or task.status = :status)
              and (:priority is null or task.priority = :priority)
              and (:title is null or lower(task.title) like lower(concat('%', :title, '%')))
              and (:dueFrom is null or task.dueDate >= :dueFrom)
              and (:dueTo is null or task.dueDate <= :dueTo)
            """)
    Page<Task> findOwnedTasks(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("title") String title,
            @Param("dueFrom") LocalDate dueFrom,
            @Param("dueTo") LocalDate dueTo,
            Pageable pageable
    );

    Optional<Task> findByIdAndUserId(Long taskId, Long userId);

    boolean existsByIdAndUserId(Long taskId, Long userId);
}
