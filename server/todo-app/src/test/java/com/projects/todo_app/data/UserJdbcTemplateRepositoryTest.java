package com.projects.todo_app.data;

import com.projects.todo_app.models.Task;
import com.projects.todo_app.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserJdbcTemplateRepositoryTest {

    @Autowired
    UserJdbcTemplateRepository repository;

    @Autowired
    TaskJdbcTemplateRepository taskRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static boolean hasSetUp = false;

    @BeforeEach
    void setup() {
        if (!hasSetUp) {
            hasSetUp = true;
            jdbcTemplate.update("call set_good_known_state();");
        }
    }

    @Test
    void shouldFindByEmail() {
        User actual = repository.findByEmail("testOne@email.com");
        assertNotNull(actual);
        assertEquals(1, actual.getUserId());
    }

    @Test
    void shouldNotFindMissingEmail() {
        User actual = repository.findByEmail("missing@missing.com");
        assertNull(actual);
    }

    @Test
    void shouldAdd() {
        User newUser = new User(0, "testNew@testNew.com", "hash1234");
        User actual = repository.add(newUser);

        assertEquals(3, actual.getUserId());
        assertEquals(newUser, actual);
    }

    @Test
    void shouldNotAddDuplicateEmail() {
        // the unique constraint on email is the last line of defence if two registrations race past UserService
        User duplicate = new User(0, "testOne@email.com", "hash1234");
        try {
            assertNull(repository.add(duplicate));
        } finally {
            // a failed insert can still consume an auto_increment id, which would shift shouldAdd's expected id
            jdbcTemplate.update("call set_good_known_state();");
        }
    }

    @Test
    void shouldDelete() {
        assertTrue(repository.deleteById(2));
    }

    @Test
    void shouldNotDeleteMissing() {
        assertFalse(repository.deleteById(999));
    }

    @Test
    void shouldDeleteCascadesToTasks() {
        User user = repository.add(new User(0, "cascade@test.com", "hash"));
        jdbcTemplate.update(
            "insert into task (user_id, title, `status`, hours, minutes) values (?, 'Cascade Task', 'NOT_STARTED', 0, 0)",
            user.getUserId());

        assertTrue(repository.deleteById(user.getUserId()));

        List<Task> tasks = taskRepository.findByUserId(user.getUserId());
        assertTrue(tasks.isEmpty());
    }
}