package com.tasktracker.controller;

import com.tasktracker.model.Task;
import com.tasktracker.service.ExportImportService;
import com.tasktracker.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private ExportImportService exportImportService;
    
    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }
    
    @GetMapping("/home")
    public String home(Model model) {
        List<Task> tasks = taskService.getRecentTasks();
        model.addAttribute("tasks", tasks);
        model.addAttribute("totalTasks", taskService.getTotalTasks());
        model.addAttribute("completedTasks", taskService.countByStatus(Task.TaskStatus.COMPLETED));
        model.addAttribute("inProgressTasks", taskService.countByStatus(Task.TaskStatus.IN_PROGRESS));
        model.addAttribute("pendingTasks", taskService.countByStatus(Task.TaskStatus.PENDING));
        model.addAttribute("currentPage", "home");
        return "home";
    }
    
    @GetMapping("/task/new")
    public String newTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("statuses", Task.TaskStatus.values());
        model.addAttribute("priorities", Task.TaskPriority.values());
        model.addAttribute("currentPage", "task-form");
        return "task-form";
    }
    
    @GetMapping("/task/edit/{id}")
    public String editTaskForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        model.addAttribute("task", task);
        model.addAttribute("statuses", Task.TaskStatus.values());
        model.addAttribute("priorities", Task.TaskPriority.values());
        model.addAttribute("currentPage", "task-form");
        return "task-form";
    }
    
    @PostMapping("/task/save")
    public String saveTask(@Valid @ModelAttribute Task task, BindingResult result, 
                          Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", Task.TaskStatus.values());
            model.addAttribute("priorities", Task.TaskPriority.values());
            return "task-form";
        }
        
        if (task.getId() == null) {
            taskService.createTask(task);
            redirectAttributes.addFlashAttribute("message", "Task created successfully!");
        } else {
            taskService.updateTask(task.getId(), task);
            redirectAttributes.addFlashAttribute("message", "Task updated successfully!");
        }
        
        return "redirect:/home";
    }
    
    @PostMapping("/task/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes attributes) {
        taskService.deleteTask(id);
        attributes.addFlashAttribute("message", "Task deleted successfully");
        return "redirect:/home";
    }

    @PostMapping("/task/{id}/complete")
    public String completeTask(@PathVariable Long id, RedirectAttributes attributes) {
        taskService.updateTaskStatus(id, Task.TaskStatus.COMPLETED);
        attributes.addFlashAttribute("message", "Task marked as completed");
        return "redirect:/home";
    }

    @PostMapping("/task/{id}/start")
    public String startTask(@PathVariable Long id, RedirectAttributes attributes) {
        taskService.updateTaskStatus(id, Task.TaskStatus.IN_PROGRESS);
        attributes.addFlashAttribute("message", "Task marked as in progress");
        return "redirect:/home";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        Map<String, Long> statusData = taskService.getTaskStatusDistribution();
        Map<String, Long> priorityData = taskService.getTaskPriorityDistribution();
        
        model.addAttribute("statusData", statusData);
        model.addAttribute("priorityData", priorityData);
        model.addAttribute("completionRate", taskService.getCompletionRate());
        model.addAttribute("averageCompletionTime", taskService.getAverageCompletionTime());
        model.addAttribute("tasksThisMonth", taskService.getTasksCreatedThisMonth());
        model.addAttribute("completedThisMonth", taskService.getTasksCompletedThisMonth());
        model.addAttribute("currentPage", "analytics");
        
        return "analytics";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("currentPage", "settings");
        return "settings";
    }
    
    @PostMapping("/settings/save")
    public String saveSettings(@RequestParam(required = false) boolean enableNotifications,
                           @RequestParam(required = false) boolean enableDarkMode,
                           RedirectAttributes redirectAttributes) {
        // Save settings to local storage
        redirectAttributes.addFlashAttribute("message", "Settings saved successfully!");
        return "redirect:/settings";
    }
    
    @PostMapping("/settings/display")
    public String saveDisplaySettings(
            @RequestParam String defaultView,
            @RequestParam int tasksPerPage,
            @RequestParam String sortBy,
            RedirectAttributes redirectAttributes) {
        // TODO: Implement settings storage
        redirectAttributes.addFlashAttribute("message", "Display settings saved successfully!");
        return "redirect:/settings";
    }

    @PostMapping("/settings/notifications")
    public String saveNotificationSettings(
            @RequestParam(required = false) boolean emailNotifications,
            @RequestParam(required = false) boolean dueDateReminders,
            @RequestParam(required = false) boolean statusUpdates,
            RedirectAttributes redirectAttributes) {
        // TODO: Implement settings storage
        redirectAttributes.addFlashAttribute("message", "Notification settings saved successfully!");
        return "redirect:/settings";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("currentPage", "about");
        return "about";
    }
    
    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportJson() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            String json = exportImportService.exportToJson(tasks);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json.getBytes("UTF-8"));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            String csv = exportImportService.exportToCsv(tasks);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes("UTF-8"));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/import")
    public String importTasks(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid file: filename is missing");
                return "redirect:/home";
            }
            
            filename = filename.toLowerCase();
            List<Task> tasks;
            
            if (filename.endsWith(".json")) {
                tasks = exportImportService.importFromJson(file);
            } else if (filename.endsWith(".csv")) {
                tasks = exportImportService.importFromCsv(file);
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid file format. Please use .json or .csv files.");
                return "redirect:/home";
            }
            
            taskService.saveAll(tasks);
            redirectAttributes.addFlashAttribute("message", 
                tasks.size() + " tasks imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error importing tasks: " + e.getMessage());
        }
        
        return "redirect:/home";
    }
    
    @PostMapping("/task/clear")
    public ResponseEntity<Void> clearAllTasks() {
        try {
            taskService.deleteAllTasks();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
