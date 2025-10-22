package com.tasktracker.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.tasktracker.model.Task;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LocalStorageService {
    private static final String DATA_DIR = "data";
    private static final String TASKS_FILE = "tasks.json";
    private final Path dataPath;
    private final Gson gson;
    private AtomicLong currentId;

    public LocalStorageService() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                @Override
                public void write(JsonWriter out, LocalDateTime value) throws IOException {
                    out.value(value != null ? value.toString() : null);
                }

                @Override
                public LocalDateTime read(JsonReader in) throws IOException {
                    String value = in.nextString();
                    return value != null ? LocalDateTime.parse(value) : null;
                }
            })
            .create();
        this.dataPath = Paths.get(DATA_DIR, TASKS_FILE);
        this.currentId = new AtomicLong(0);
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            if (!Files.exists(dataPath)) {
                Files.createFile(dataPath);
                saveTasksToFile(new ArrayList<>());
            }
            
            // Initialize currentId based on existing tasks
            List<Task> tasks = loadTasksFromFile();
            if (!tasks.isEmpty()) {
                long maxId = tasks.stream()
                    .mapToLong(Task::getId)
                    .max()
                    .orElse(0);
                currentId.set(maxId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    private List<Task> loadTasksFromFile() {
        try {
            String content = Files.readString(dataPath);
            if (content.isEmpty()) {
                return new ArrayList<>();
            }
            Type listType = new TypeToken<List<Task>>(){}.getType();
            return gson.fromJson(content, listType);
        } catch (IOException e) {
            throw new RuntimeException("Could not read tasks from file", e);
        }
    }

    private void saveTasksToFile(List<Task> tasks) {
        try {
            String json = gson.toJson(tasks);
            Files.writeString(dataPath, json);
        } catch (IOException e) {
            throw new RuntimeException("Could not save tasks to file", e);
        }
    }

    public List<Task> getAllTasks() {
        return loadTasksFromFile();
    }

    public Optional<Task> getTaskById(Long id) {
        return loadTasksFromFile().stream()
            .filter(task -> task.getId().equals(id))
            .findFirst();
    }

    public Task saveTask(Task task) {
        List<Task> tasks = loadTasksFromFile();
        if (task.getId() == null) {
            task.setId(currentId.incrementAndGet());
            tasks.add(task);
        } else {
            int index = findTaskIndex(tasks, task.getId());
            if (index != -1) {
                tasks.set(index, task);
            } else {
                tasks.add(task);
            }
        }
        saveTasksToFile(tasks);
        return task;
    }

    public void deleteTask(Long id) {
        List<Task> tasks = loadTasksFromFile();
        tasks.removeIf(task -> task.getId().equals(id));
        saveTasksToFile(tasks);
    }

    private int findTaskIndex(List<Task> tasks, Long id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}