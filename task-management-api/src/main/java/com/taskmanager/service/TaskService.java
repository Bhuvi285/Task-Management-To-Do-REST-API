package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
        TaskRepository taskRepository,
        UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Task createTask(
        Long userId,
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException(
                "User not found with id: " + userId
            ));

        Task task = new Task(title, description, status, dueDate, user);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Task not found with id: " + taskId
            ));
    }

    @Transactional
    public Task updateTask(
        Long taskId,
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate
    ) {
        Task task = getTaskById(taskId);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);
        return task;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getTaskById(taskId);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return taskRepository.findByUserId(userId);
    }
}
