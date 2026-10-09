package com.projects.todo_app.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class User {

    private int userId;
    @NotBlank(message = "email cannot be blank")
    @NotNull(message = "email cannot be blank")
    private String email;
    @NotBlank(message = "password cannot be blank")
    private String password;

    public User() {} // used by HTTP requests

    public User(String email) {
        this.email = email;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(int userId, String email, String password) {
        this.userId = userId;
        this.email = email;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId && Objects.equals(email, user.email);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(userId, email);
    }
}
