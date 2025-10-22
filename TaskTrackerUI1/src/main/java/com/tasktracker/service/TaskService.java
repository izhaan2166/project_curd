package com.tasktracker.service;

import com.tasktracker.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    
    @Autowired
    private LocalStorageService storageService;
    
    public List<Task> getAllTasks() {
        return storageService.getAllTasks();
    }
    
    public List<Task> getRecentTasks() {
        return storageService.getAllTasks().stream()
            .sorted(Comparator.comparing(Task::getCreatedAt).reversed())
            .collect(Collectors.toList());
    }
    
    public Optional<Task> getTaskById(Long id) {
        return storageService.getTaskById(id);
    }
    
    public Task createTask(Task task) {
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return storageService.saveTask(task);
    }
    
    public Task updateTask(Long id, Task taskDetails) {
        Task task = storageService.getTaskById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setPriority(taskDetails.getPriority());
        task.setDueDate(taskDetails.getDueDate());
        task.setUpdatedAt(LocalDateTime.now());
        
        return storageService.saveTask(task);
    }
    
    public void deleteTask(Long id) {
        storageService.deleteTask(id);
    }

    public long getTotalTasks() {
        return storageService.getAllTasks().size();
    }

    public long countByStatus(Task.TaskStatus status) {
        return storageService.getAllTasks().stream()
            .filter(task -> task.getStatus() == status)
            .count();
    }
    
    public void deleteAllTasks() {
        storageService.getAllTasks().forEach(task -> 
            storageService.deleteTask(task.getId()));
    }
    
    public void saveAll(List<Task> tasks) {
        tasks.forEach(task -> storageService.saveTask(task));
    }

    public Map<String, Long> getTaskStatusDistribution() {
        List<Task> tasks = getAllTasks();
        return tasks.stream()
            .collect(Collectors.groupingBy(
                task -> task.getStatus().toString(),
                Collectors.counting()
            ));
    }

    public Map<String, Long> getTaskPriorityDistribution() {
        List<Task> tasks = getAllTasks();
        return tasks.stream()
            .collect(Collectors.groupingBy(
                task -> task.getPriority().toString(),
                Collectors.counting()
            ));
    }

    public double getCompletionRate() {
        List<Task> tasks = getAllTasks();
        if (tasks.isEmpty()) return 0.0;
        
        long completed = tasks.stream()
            .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
            .count();
        
        return Math.round((double) completed / tasks.size() * 100);
    }

    public String getAverageCompletionTime() {
        List<Task> completedTasks = getAllTasks().stream()
            .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED && 
                          task.getCompletedAt() != null)
            .collect(Collectors.toList());
        
        if (completedTasks.isEmpty()) return "N/A";
        
        double avgDays = completedTasks.stream()
            .mapToLong(task -> ChronoUnit.DAYS.between(task.getCreatedAt(), task.getCompletedAt()))
            .average()
            .orElse(0.0);
        
        return String.format("%.1f days", avgDays);
    }

    public long getTasksCreatedThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        return getAllTasks().stream()
            .filter(task -> task.getCreatedAt().isAfter(startOfMonth))
            .count();
    }

    public long getTasksCompletedThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        return getAllTasks().stream()
            .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED &&
                          task.getCompletedAt() != null &&
                          task.getCompletedAt().isAfter(startOfMonth))
            .count();
    }

    public List<Map<String, String>> getRecentActivity() {
        List<Task> tasks = getAllTasks();
        List<Map<String, String>> activities = new ArrayList<>();
        
        // Add recent task creations
        tasks.stream()
            .sorted(Comparator.comparing(Task::getCreatedAt).reversed())
            .limit(5)
            .forEach(task -> {
                Map<String, String> activity = new HashMap<>();
                activity.put("description", "Created task: " + task.getTitle());
                activity.put("date", task.getCreatedAt().toString());
                activities.add(activity);
            });
        
        // Add recent task completions
        tasks.stream()
            .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED && task.getCompletedAt() != null)
            .sorted(Comparator.comparing(Task::getCompletedAt).reversed())
            .limit(5)
            .forEach(task -> {
                Map<String, String> activity = new HashMap<>();
                activity.put("description", "Completed task: " + task.getTitle());
                activity.put("date", task.getCompletedAt().toString());
                activities.add(activity);
            });
        
        // Sort all activities by date
        activities.sort((a1, a2) -> 
            LocalDateTime.parse(a2.get("date")).compareTo(LocalDateTime.parse(a1.get("date"))));
        
        return activities.stream().limit(10).collect(Collectors.toList());
    }
    public Task updateTaskStatus(Long id, Task.TaskStatus status) {
        Task task = getTaskById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        if (status == Task.TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        return storageService.saveTask(task);
    }
}
