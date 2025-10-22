package com.tasktracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opencsv.CSVWriter;
import com.tasktracker.model.Task;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportImportService {
    
    private final ObjectMapper objectMapper;
    
    public ExportImportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public String exportToJson(List<Task> tasks) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tasks);
        } catch (Exception e) {
            throw new RuntimeException("Error exporting to JSON: " + e.getMessage());
        }
    }
    
    public String exportToCsv(List<Task> tasks) {
        try {
            StringWriter sw = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(sw);
            
            String[] header = {"ID", "Title", "Description", "Status", "Priority", "Created At", "Due Date"};
            csvWriter.writeNext(header);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (Task task : tasks) {
                String[] row = {
                    task.getId() != null ? task.getId().toString() : "",
                    task.getTitle(),
                    task.getDescription() != null ? task.getDescription() : "",
                    task.getStatus().toString(),
                    task.getPriority().toString(),
                    task.getCreatedAt().format(formatter),
                    task.getDueDate() != null ? task.getDueDate().format(formatter) : ""
                };
                csvWriter.writeNext(row);
            }
            
            csvWriter.close();
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error exporting to CSV: " + e.getMessage());
        }
    }
    
    public List<Task> importFromJson(MultipartFile file) {
        try {
            String content = new String(file.getBytes());
            return objectMapper.readValue(content, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
        } catch (Exception e) {
            throw new RuntimeException("Error importing from JSON: " + e.getMessage());
        }
    }
    
    public List<Task> importFromCsv(MultipartFile file) {
        List<Task> tasks = new ArrayList<>();
        try {
            com.opencsv.CSVReader csvReader = new com.opencsv.CSVReader(
                new InputStreamReader(file.getInputStream())
            );
            
            String[] header = csvReader.readNext();
            String[] line;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            while ((line = csvReader.readNext()) != null) {
                if (line.length >= 4) {
                    Task task = new Task();
                    task.setTitle(line[1]);
                    if (line.length > 2 && !line[2].isEmpty()) {
                        task.setDescription(line[2]);
                    }
                    task.setStatus(Task.TaskStatus.valueOf(line[3]));
                    if (line.length > 4 && !line[4].isEmpty()) {
                        task.setPriority(Task.TaskPriority.valueOf(line[4]));
                    }
                    if (line.length > 5 && !line[5].isEmpty()) {
                        task.setCreatedAt(LocalDateTime.parse(line[5], formatter));
                    }
                    if (line.length > 6 && !line[6].isEmpty()) {
                        task.setDueDate(LocalDateTime.parse(line[6], formatter));
                    }
                    tasks.add(task);
                }
            }
            csvReader.close();
        } catch (Exception e) {
            throw new RuntimeException("Error importing from CSV: " + e.getMessage());
        }
        return tasks;
    }
}
