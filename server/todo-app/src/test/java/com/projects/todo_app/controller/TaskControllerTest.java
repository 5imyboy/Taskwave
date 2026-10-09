package com.projects.todo_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.todo_app.data.TaskRepository;
import com.projects.todo_app.models.Status;
import com.projects.todo_app.models.Task;
import com.projects.todo_app.models.User;
import com.projects.todo_app.security.JwtConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // 2. Configure the mock MVC environment.
class TaskControllerTest {

    @Mock
    TaskRepository repository;

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JwtConverter jwtConverter;

    private String token;
    private static boolean hasSetUp = false;

    private final List<Task> TASKS = List.of(
            new Task(1, 1, "Clean Room", "Make the bed and vacumm the floor", Status.NOT_STARTED, 2, 0),
            new Task(2, 1, "Brush Teeth", "Brush for at least 3 minutes!", Status.IN_PROGRESS, 0, 10),
            new Task(3, 1, "Wake Up", "", Status.COMPLETED, 0, 5)
    );

    private final List<User> USERS = List.of(
            new User(1, "testOne@email.com", "hash1234"),
            new User(2, "testTwo@email.com", "hash5678")
    );

    @BeforeEach
    void setup() {
        if (!hasSetUp) {
            hasSetUp = true;
            jdbcTemplate.update("call set_good_known_state();");
        }
        token = jwtConverter.getTokenFromUser(new User(USERS.get(0).getEmail()));
    }

    @Test
    void shouldFindTasksForAuthenticatedUser() throws Exception {
        String result = mvc.perform(get("/api/task")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        // all returned tasks should belong to the authenticated user (userId = 1)
        ObjectMapper mapper = new ObjectMapper();
        Task[] tasks = mapper.readValue(result, Task[].class);
        for (Task task : tasks) {
            assertEquals(1, task.getUserId());
        }
    }

    @Test
    void shouldFindById() throws Exception {
        Task task = TASKS.get(0);

        ObjectMapper jsonMapper = new ObjectMapper();
        String expectedJson = jsonMapper.writeValueAsString(task);

        when(repository.findById(1)).thenReturn(task);

        mvc.perform(get("/api/task/id/1")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson, JsonCompareMode.LENIENT));
    }

    @Test
    void shouldNotFindMissingId() throws Exception {
        when(repository.findById(999)).thenReturn(null);

        mvc.perform(get("/api/task/id/999")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindBySection() throws Exception {
        when(repository.findByUserIdAndStatus(1, Status.COMPLETED)).thenReturn(List.of(TASKS.get(2)));

        String result = mvc.perform(get("/api/task/status/completed")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        Task[] tasks = mapper.readValue(result, Task[].class);
        for (Task task : tasks) {
            assertEquals(1, task.getUserId());
            assertEquals(Status.COMPLETED, task.getStatus());
        }
    }

    @Test
    void shouldFindByEachStatus() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String[] paths = {"not-started", "in-progress", "completed"};
        Status[] statuses = {Status.NOT_STARTED, Status.IN_PROGRESS, Status.COMPLETED};

        for (int i = 0; i < paths.length; i++) {
            String result = mvc.perform(get("/api/task/status/" + paths[i])
                            .cookie(new Cookie("token", token)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Task[] tasks = mapper.readValue(result, Task[].class);
            for (Task task : tasks) {
                assertEquals(1, task.getUserId());
                assertEquals(statuses[i], task.getStatus());
            }
        }
    }

    @Test
    void shouldNotFindByInvalidStatus() throws Exception {
        mvc.perform(get("/api/task/status/done")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isBadRequest());

        // enum names are not accepted — only the hyphenated path form
        mvc.perform(get("/api/task/status/COMPLETED")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAdd() throws Exception {
        Task newTaskIn = new Task(0, 99, "Make Breakfast", "", Status.NOT_STARTED, 0, 20);

        ObjectMapper mapper = new ObjectMapper();
        String newTaskJson = mapper.writeValueAsString(newTaskIn);

        String result = mvc.perform(post("/api/task/add")
                        .cookie(new Cookie("token", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newTaskJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        // userId should be set from the authenticated user, not from the request body (which sent 99)
        Task created = mapper.readValue(result, Task.class);
        assertEquals(1, created.getUserId());
    }

    @Test
    void shouldNotAddInvalid() throws Exception {
        Task nullTask = null;
        Task blankTitle = new Task(0, 1, "", "", Status.NOT_STARTED, 0, 20);
        Task nullStatus = new Task(0, 1, "Go for a Walk", "", null, 0, 20);
        Task emptyTime = new Task(0, 1, "Go for a Walk", "", Status.NOT_STARTED, 0, 0);

        ObjectMapper mapper = new ObjectMapper();
        String nullTaskJson = mapper.writeValueAsString(nullTask);
        String blankTitleJson = mapper.writeValueAsString(blankTitle);
        String nullStatusJson = mapper.writeValueAsString(nullStatus);
        String emptyTimeJson = mapper.writeValueAsString(emptyTime);

        var request = post("/api/task/add")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullTaskJson);
        mvc.perform(request).andExpect(status().isBadRequest());

        request = post("/api/task/add")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(blankTitleJson);
        mvc.perform(request).andExpect(status().isBadRequest());

        request = post("/api/task/add")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullStatusJson);
        mvc.perform(request).andExpect(status().isBadRequest());

        request = post("/api/task/add")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyTimeJson);
        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdate() throws Exception {
        Task newTask = new Task(1, 1, "Wash Dishes", "", Status.NOT_STARTED, 0, 30);

        ObjectMapper mapper = new ObjectMapper();
        String newTaskJson = mapper.writeValueAsString(newTask);

        var request = put("/api/task/update/1")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(newTaskJson);

        mvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    void shouldNotUpdateInvalid() throws Exception {
        Task nullTask = null;
        Task blankTitle = new Task(1, 1, "", "", Status.NOT_STARTED, 0, 30);
        Task nullStatus = new Task(1, 1, "Missing Status", "", null, 0, 30);
        Task emptyTime = new Task(1, 1, "Missing Time", "", Status.NOT_STARTED, 0, 0);
        Task missingId = new Task(99,1,  "Missing Id", "", Status.NOT_STARTED, 0, 30);

        ObjectMapper mapper = new ObjectMapper();
        String nullTaskJson = mapper.writeValueAsString(nullTask);
        String blankTitleJson = mapper.writeValueAsString(blankTitle);
        String nullStatusJson = mapper.writeValueAsString(nullStatus);
        String emptyTimeJson = mapper.writeValueAsString(emptyTime);
        String missingIdJson = mapper.writeValueAsString(missingId);

        var request = put("/api/task/update/1")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullTaskJson);
        mvc.perform(request).andExpect(status().isBadRequest());

        request = put("/api/task/update/1")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(blankTitleJson);
        mvc.perform(request).andExpect(status().isBadRequest());

        request = put("/api/task/update/1")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(nullStatusJson);
        mvc.perform(request).andExpect(status().isBadRequest());

        request = put("/api/task/update/1")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyTimeJson);
        mvc.perform(request).andExpect(status().isBadRequest());

        request = put("/api/task/update/99")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingIdJson);
        mvc.perform(request).andExpect(status().isNotFound());

        request = put("/api/task/update/1")
                .cookie(new Cookie("token", token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(missingIdJson);
        mvc.perform(request).andExpect(status().isConflict());
    }

    @Test
    void shouldDelete() throws Exception {
        mvc.perform(delete("/api/task/delete/2")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldNotDeleteMissing() throws Exception {
        mvc.perform(delete("/api/task/delete/999")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isNotFound());
    }
}