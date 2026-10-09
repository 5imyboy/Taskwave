package com.projects.todo_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.todo_app.data.UserRepository;
import com.projects.todo_app.models.User;
import com.projects.todo_app.security.JwtConverter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // 2. Configure the mock MVC environment.
class UserControllerTest {

    @Mock
    UserRepository repository;

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JwtConverter jwtConverter;

    private static boolean hasSetUp = false;
    private String token;

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
    void shouldFindByEmail() throws Exception {
        User user = USERS.get(0);

        ObjectMapper jsonMapper = new ObjectMapper();
        String expectedJson = jsonMapper.writeValueAsString(user);

        mvc.perform(get("/api/user/" + user.getEmail())
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotFindMissingEmail() throws Exception {
        mvc.perform(get("/api/user/null@null.com")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUserAndCascadeTasks() throws Exception {
        // use a throwaway user so other tests in this class keep users 1 and 2
        String email = "deleteMe@email.com";
        jdbcTemplate.update("insert into `user` (email, password_hash) values (?, ?);", email, "hash");
        Integer userId = jdbcTemplate.queryForObject(
                "select user_id from `user` where email = ?;", Integer.class, email);
        jdbcTemplate.update(
                "insert into task (user_id, title, `description`, `status`, hours, minutes) values (?, ?, '', 'NOT_STARTED', 0, 10);",
                userId, "Task Owned By Deleted User");

        String deleteToken = jwtConverter.getTokenFromUser(new User(email));
        mvc.perform(delete("/api/user/delete/" + userId)
                        .cookie(new Cookie("token", deleteToken)))
                .andExpect(status().isNoContent());

        Integer users = jdbcTemplate.queryForObject(
                "select count(*) from `user` where user_id = ?;", Integer.class, userId);
        Integer tasks = jdbcTemplate.queryForObject(
                "select count(*) from task where user_id = ?;", Integer.class, userId);
        assertEquals(0, users);
        assertEquals(0, tasks);
    }

    @Test
    void shouldNotDeleteMissingUser() throws Exception {
        mvc.perform(delete("/api/user/delete/999")
                        .cookie(new Cookie("token", token)))
                .andExpect(status().isNotFound());
    }
}