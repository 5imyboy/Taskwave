package com.projects.todo_app.domain;

import com.projects.todo_app.data.TaskRepository;
import com.projects.todo_app.models.Status;
import com.projects.todo_app.models.Task;
import jakarta.validation.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public List<Task> findByUserId(int userId) {
        return repository.findByUserId(userId);
    }

    public List<Task> findByUserIdAndStatus(int userId, Status status) {
        return repository.findByUserIdAndStatus(userId, status);
    }

    public Task findById(int taskId) {
        return repository.findById(taskId);
    }

    public Result<Task> add(Task task) {
        Result<Task> result = new Result<>();

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Task>> violations = validator.validate(task);

        if (!violations.isEmpty()) {
            for (ConstraintViolation<Task> violation: violations) {
                result.addErrorMessage(violation.getMessage());
            }
            return result;
        }

        // I can't figure out how to use annotations to configure this piece of validation
        if (!isTitleUnique(task.getTitle(), task.getUserId())) {
            result.addErrorMessage("Title must be unique");
            return result;
        }

        result.setPayLoad(repository.add(task));
        return result;

    }

    public Result<Task> update(Task task) {
        Result<Task> result = new Result<>();

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Task>> violations = validator.validate(task);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Task> violation: violations) {
                result.addErrorMessage(violation.getMessage());
            }
            return result;
        }

        if (!isTitleUnique(task.getTitle(), task.getUserId(), task.getTaskId())) {
            result.addErrorMessage("Title must be unique");
            return result;
        }

        if (!repository.update(task)) {
            result.addMessage(
                    String.format("task id: %s, not found", task.getTaskId()),
                    ResultType.NOT_FOUND
            );
            return result;
        }

        result.setPayLoad(task);
        return result;
    }

    public boolean deleteById(long taskId) {
        return repository.deleteById(taskId);
    }


    // Validation helper functions
    // Titles only need to be unique within one user's tasks (matches uq_task_user_title in the schema).
    private boolean isTitleUnique(String title, int userId) {
        return isTitleUnique(title, userId, -1);
    }

    private boolean isTitleUnique(String title, int userId, int idToIgnore) {
        List<Task> tasks = findByUserId(userId);
        for (Task task: tasks) {
            if (task.getTaskId() == idToIgnore) continue;
            if (task.getTitle().equals(title)) {
                return false;
            }
        }
        return true;
    }

}
