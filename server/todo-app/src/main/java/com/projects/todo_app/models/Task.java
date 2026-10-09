package com.projects.todo_app.models;

import com.projects.todo_app.domain.custom_validation.TimeExists;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@TimeExists(message = "Time cannot be blank")
public class Task {
    private int taskId;
    private int userId;
    @NotBlank(message = "Task title cannot be blank")
    private String title;
    private String description;
    @NotNull(message = "Task status cannot be blank")
    private Status status;
    private int hours;
    private int minutes;

    public Task() {}

    public Task(int taskId, int userId, String title, String description, Status status, int hours, int minutes) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.hours = hours;
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUserId() { return userId; }

    public void setUserId(int userId) { this.userId = userId; }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId == task.taskId && hours == task.hours && minutes == task.minutes && Objects.equals(title, task.title) && Objects.equals(description, task.description) && status == task.status;
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(taskId, title, description, status, hours, minutes);
    }
}
