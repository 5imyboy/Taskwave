package com.projects.todo_app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.todo_app.models.Task;
import com.projects.todo_app.models.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Exercises the filter through a real request so the whole security chain is involved.
// Only GET /api/task is used, so these tests never modify the database.
@SpringBootTest
@AutoConfigureMockMvc
class JwtRequestFilterTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JwtConverter jwtConverter;

    private static boolean hasSetUp = false;
    private String userOneToken;
    private String userTwoToken;

    @BeforeEach
    void setup() {
        if (!hasSetUp) {
            hasSetUp = true;
            jdbcTemplate.update("call set_good_known_state();");
        }
        userOneToken = jwtConverter.getTokenFromUser(new User("testOne@email.com"));
        userTwoToken = jwtConverter.getTokenFromUser(new User("testTwo@email.com"));
    }

    @Test
    void shouldAuthenticateWithBearerHeader() throws Exception {
        mvc.perform(get("/api/task")
                        .header("Authorization", "Bearer " + userOneToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAuthenticateWithCookie() throws Exception {
        mvc.perform(get("/api/task")
                        .cookie(new Cookie("token", userOneToken)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectMissingToken() throws Exception {
        mvc.perform(get("/api/task"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectNonBearerHeader() throws Exception {
        mvc.perform(get("/api/task")
                        .header("Authorization", "Basic " + userOneToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCookiesWithoutToken() throws Exception {
        mvc.perform(get("/api/task")
                        .cookie(new Cookie("user_email", "testOne@email.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidBearerToken() throws Exception {
        mvc.perform(get("/api/task")
                        .header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidCookieToken() throws Exception {
        mvc.perform(get("/api/task")
                        .cookie(new Cookie("token", "not.a.jwt")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldPreferHeaderOverCookie() throws Exception {
        // header identifies user 2 (no tasks), cookie identifies user 1 (3 tasks)
        String result = mvc.perform(get("/api/task")
                        .header("Authorization", "Bearer " + userTwoToken)
                        .cookie(new Cookie("token", userOneToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Task[] tasks = new ObjectMapper().readValue(result, Task[].class);
        assertEquals(0, tasks.length);
    }
}
