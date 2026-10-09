package com.projects.todo_app.data;

import com.projects.todo_app.models.Status;
import com.projects.todo_app.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TaskJdbcTemplateRepositoryTest {

    @Autowired
    TaskJdbcTemplateRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static boolean hasSetUp = false;
    private final int SIZE = 3;

    @BeforeEach
    void setup() {
        if (!hasSetUp) {
            hasSetUp = true;
            jdbcTemplate.update("call set_good_known_state();");
        }
    }

    @Test
    void shouldFindAll() {
        List<Task> actual = repository.findAll();
        assertNotNull(actual);
        assertTrue(SIZE-1 <= actual.size() && actual.size() <= SIZE+1);

        for (Task task: actual) {
            assertNotEquals(0, task.getTaskId());
            assertNotNull(task.getTitle());
            assertNotNull(task.getStatus());
        }
    }

    @Test
    void shouldFindByUserId() {
        List<Task> actual = repository.findByUserId(1);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());

        for (Task task : actual) {
            assertEquals(1, task.getUserId());
        }
    }

    @Test
    void shouldFindByUserIdReturnsEmpty() {
        List<Task> actual = repository.findByUserId(999);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    void shouldFindByUserIdAndStatus() {
        List<Task> actual = repository.findByUserIdAndStatus(1, Status.IN_PROGRESS);
        assertNotNull(actual);

        for (Task task: actual) {
            assertNotEquals(0, task.getTaskId());
            assertNotNull(task.getTitle());
            assertEquals(1, task.getUserId());
            assertEquals(Status.IN_PROGRESS, task.getStatus());
        }
    }

    @Test
    void shouldFindById() {
        Task actual = repository.findById(1);
        assertNotNull(actual);
        assertEquals(1, actual.getTaskId());
    }

    @Test
    void shouldNotFindMissingId() {
        Task actual = repository.findById(999);
        assertNull(actual);
    }

    @Test
    void shouldAdd() {
        Task newTask = new Task(0, 1, "Make Breakfast", "", Status.NOT_STARTED, 0, 20);
        Task actual = repository.add(newTask);

        assertEquals(SIZE+1, actual.getTaskId());
        assertEquals(newTask, actual);
    }

    @Test
    void shouldNotAddNull() {
        Task nullTask = new Task();
        Task actual = repository.add(nullTask);

        assertNull(actual);
    }

    @Test
    void shouldNotAddDuplicateTitleForSameUser() {
        // user 1 already has "Wake Up" — the uq_task_user_title constraint rejects the insert
        Task duplicate = new Task(0, 1, "Wake Up", "", Status.NOT_STARTED, 0, 5);
        try {
            assertNull(repository.add(duplicate));
        } finally {
            // a failed insert can still consume an auto_increment id, which would shift shouldAdd's expected id
            jdbcTemplate.update("call set_good_known_state();");
        }
    }

    @Test
    void shouldAddSameTitleForDifferentUser() {
        // user 2 can use a title that user 1 already has
        Task sameTitle = new Task(0, 2, "Wake Up", "", Status.NOT_STARTED, 0, 5);
        try {
            Task actual = repository.add(sameTitle);
            assertNotNull(actual);
            assertEquals(2, actual.getUserId());
        } finally {
            jdbcTemplate.update("call set_good_known_state();");
        }
    }

    @Test
    void shouldUpdate() {
        Task newTask = new Task(1, 1, "Wash Dishes", "", Status.NOT_STARTED, 0, 30);
        assertTrue(repository.update(newTask));
        Task actual = repository.findById(1);

        assertEquals(newTask, actual);
    }

    @Test
    void shouldNotUpdateMissing() {
        Task missingTask = new Task(99, 1, "Wash Dishes", "", Status.NOT_STARTED, 0, 30);
        assertFalse(repository.update(missingTask));
    }

    @Test
    void shouldDelete() {
        assertTrue(repository.deleteById(2));
    }

    @Test
    void shouldNotDeleteMissing() {
        assertFalse(repository.deleteById(99));
    }
}