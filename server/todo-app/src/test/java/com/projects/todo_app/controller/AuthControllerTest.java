package com.projects.todo_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.todo_app.models.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

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
    void shouldLoginWithValidCredentials() throws Exception {
        Map<String, String> credentials = Map.of("email", "testOne@email.com", "password", "password");
        ObjectMapper mapper = new ObjectMapper();

        MockHttpServletResponse response = mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // response body should contain the user
        String body = response.getContentAsString();
        assertTrue(body.contains("testOne@email.com"));

        // both HttpOnly cookies should be set
        Cookie tokenCookie = response.getCookie("token");
        Cookie emailCookie = response.getCookie("user_email");
        assertNotNull(tokenCookie);
        assertNotNull(emailCookie);
        assertTrue(tokenCookie.isHttpOnly());
        assertTrue(emailCookie.isHttpOnly());
        assertEquals("testOne@email.com", emailCookie.getValue());
    }

    @Test
    void shouldNotLoginWithInvalidCredentials() throws Exception {
        Map<String, String> credentials = Map.of("email", "testOne@email.com", "password", "wrongpassword");
        ObjectMapper mapper = new ObjectMapper();

        mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotLoginWithUnknownEmail() throws Exception {
        Map<String, String> credentials = Map.of("email", "nobody@email.com", "password", "password");
        ObjectMapper mapper = new ObjectMapper();

        mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRegister() throws Exception {
        User newUser = new User(0, "newUser@email.com", "password");
        ObjectMapper mapper = new ObjectMapper();

        String result = mvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertTrue(result.contains("newUser@email.com"));
    }

    @Test
    void shouldNotRegisterDuplicateEmail() throws Exception {
        User duplicate = new User(0, "testOne@email.com", "password");
        ObjectMapper mapper = new ObjectMapper();

        mvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotRegisterMissingFields() throws Exception {
        User missingEmail = new User(0, "", "password");
        User missingPassword = new User(0, "another@email.com", "");
        ObjectMapper mapper = new ObjectMapper();

        mvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(missingEmail)))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(missingPassword)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLogout() throws Exception {
        MockHttpServletResponse response = mvc.perform(post("/api/logout"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // cookies should be cleared (maxAge = 0)
        Cookie tokenCookie = response.getCookie("token");
        Cookie emailCookie = response.getCookie("user_email");
        assertNotNull(tokenCookie);
        assertNotNull(emailCookie);
        assertEquals(0, tokenCookie.getMaxAge());
        assertEquals(0, emailCookie.getMaxAge());
    }
}
