package com.tasktracker.controller;

import com.tasktracker.model.Task;
import com.tasktracker.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {
    
    @Autowired
    private TaskService taskService;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        model.addAttribute("currentPage", "profile");
        model.addAttribute("tasksCreated", taskService.getTotalTasks());
        model.addAttribute("tasksCompleted", taskService.countByStatus(Task.TaskStatus.COMPLETED));
        model.addAttribute("completionRate", taskService.getCompletionRate());
        return "profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                              @RequestParam String email,
                              RedirectAttributes redirectAttributes) {
        // In a real application, you would save these to a database
        // For now, we'll just show a success message
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
        return "redirect:/profile";
    }
}