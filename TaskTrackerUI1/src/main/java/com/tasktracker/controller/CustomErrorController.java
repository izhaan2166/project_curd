package com.tasktracker.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            model.addAttribute("status", statusCode);
            model.addAttribute("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
            
            if (message != null && !message.toString().isEmpty()) {
                model.addAttribute("message", message);
            } else {
                if (statusCode == 404) {
                    model.addAttribute("message", "The page you're looking for doesn't exist.");
                } else if (statusCode == 500) {
                    model.addAttribute("message", "An internal server error occurred. Please try again later.");
                } else {
                    model.addAttribute("message", "An unexpected error occurred.");
                }
            }
        } else {
            model.addAttribute("status", "Error");
            model.addAttribute("error", "Unknown Error");
            model.addAttribute("message", "An unexpected error occurred.");
        }
        
        model.addAttribute("currentPage", "error");
        return "error";
    }
}