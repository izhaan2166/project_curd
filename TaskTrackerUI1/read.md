# Task Tracker Application

## Overview
A full-featured Spring Boot task management application with CRUD operations, data import/export, and a clean, modern flat design.

## Recent Changes (October 17, 2025)
- Created complete CRUD functionality for tasks (Create, Read, Update, Delete)
- Implemented PostgreSQL database integration with Spring Data JPA
- Added data export functionality (JSON and CSV formats)
- Added data import functionality (JSON and CSV formats)
- Implemented full-screen welcome page with gradient background
- Created dashboard with real-time task statistics
- Removed card-style design for clean, minimalist flat UI
- Added responsive design with proper spacing

## Features
### Core Functionality
- ✅ Create new tasks with title, description, status, priority, and due date
- ✅ View all tasks in a clean list view
- ✅ Edit existing tasks
- ✅ Delete tasks with confirmation
- ✅ Real-time task statistics (Total, Completed, In Progress, Pending)
- ✅ Export tasks to JSON or CSV
- ✅ Import tasks from JSON or CSV files

### Task Properties
- Title (required)
- Description (optional)
- Status: PENDING, IN_PROGRESS, COMPLETED
- Priority: LOW, MEDIUM, HIGH
- Due Date (optional)
- Created/Updated timestamps

## Project Architecture
- **Backend**: Spring Boot 3.2.0 with Java 17+
- **Database**: PostgreSQL (Neon)
- **ORM**: Hibernate/JPA
- **Frontend**: Thymeleaf templates with custom CSS
- **Build Tool**: Maven
- **Server Port**: 5000

## Structure
```
src/
├── main/
│   ├── java/com/tasktracker/
│   │   ├── TaskTrackerApplication.java (Main application)
│   │   ├── controller/
│   │   │   └── TaskController.java (All routes and CRUD operations)
│   │   ├── model/
│   │   │   └── Task.java (JPA Entity)
│   │   ├── repository/
│   │   │   └── TaskRepository.java (JPA Repository)
│   │   └── service/
│   │       ├── TaskService.java (Business logic)
│   │       └── ExportImportService.java (Import/Export logic)
│   └── resources/
│       ├── application.properties (Database & app configuration)
│       ├── static/css/
│       │   └── style.css (All styling)
│       └── templates/
│           ├── welcome.html (Full-screen landing page)
│           ├── home.html (Dashboard with tasks and actions)
│           └── task-form.html (Create/Edit task form)
```

## Routes
- `/` - Welcome page
- `/home` - Dashboard with task list and statistics
- `/task/new` - Create new task form
- `/task/edit/{id}` - Edit task form
- `/task/save` - Save task (POST)
- `/task/delete/{id}` - Delete task (POST)
- `/export/json` - Export tasks as JSON
- `/export/csv` - Export tasks as CSV
- `/import` - Import tasks (POST)

## Design Features
- Full-screen welcome page with gradient purple background
- Clean flat UI with no card-style elements
- Proper spacing and gaps between all elements
- Color-coded task statuses and priorities
- Responsive grid layout for statistics
- Modal dialogs for import functionality
- Dropdown menus for export options
- Mobile-responsive design
