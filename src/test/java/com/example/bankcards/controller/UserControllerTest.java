package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.UserUpdateRequest;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected UserService userService;

    private UserResponse testUserResponse() {
        return UserResponse.builder()
                            .id(1L)
                            .name("Иван")
                            .email("admin@example.com")
                            .build();
    }

    private Page<UserResponse> createTestPage() {
        return new PageImpl<>(Collections.singletonList(testUserResponse()));
    }

    private UserCreateRequest testUserCreateRequest() {
        return new UserCreateRequest("Дмитрий", "dima@example.com", "password", UserRole.USER);
    }

    private UserUpdateRequest testUserUpdateRequest() {
        return UserUpdateRequest.builder()
                .email("new@exmapl.com")
                .build();
    }

    // GET /api/users
    @Test
    void getAllUsersWithAdminRole_thenOk() throws Exception {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(createTestPage());

        mockMvc.perform(get("/api/users")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].email").value("admin@example.com"))
                .andExpect(jsonPath("$.content[0].name").value("Иван"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllUsersWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(user("1").roles(UserRole.USER.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsersWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // GET /api/users/{id}
    @Test
    void getUserByIdWithAdminRole_thenOk() throws Exception {
        when(userService.getUserById(1)).thenReturn(testUserResponse());

        mockMvc.perform(get("/api/users/1")
                        .with(user("1").roles(UserRole.ADMIN.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.name").value("Иван"));

    }

    @Test
    void getUserByIdWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/users/1")
                        .with(user("1").roles(UserRole.USER.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserByIdWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    // POST /api/users
    @Test
    void createUserWithAdminRole_thenOk() throws Exception {
        UserResponse response = UserResponse.builder()
                        .id(2L)
                        .name("Дмитрий")
                        .email("dima@example.com")
                        .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"name\":\"Дмитрий\"," +
                                "\"email\":\"dima@example.com\", " +
                                "\"password\":\"password\", \"role\":\"USER\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("dima@example.com"))
                .andExpect(jsonPath("$.name").value("Дмитрий"));
    }

    @Test
    void createUserWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(user("1").roles(UserRole.USER.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUserWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }

    // PUT /api/users/1
    @Test
    void updateUserWithAdminRole_thenOk() throws Exception {
        UserUpdateRequest request = testUserUpdateRequest();
        UserResponse response = testUserResponse();
        response.setEmail("new@example.com");

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .with(user("123").roles(UserRole.ADMIN.toString()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"new@example.com\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.name").value("Иван"));
    }

    @Test
    void updateUserWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .with(user("1").roles(UserRole.USER.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // DELETE /api/users/1
    @Test
    void deleteUserIdWithAdminRole_thenOk() throws Exception {
        doNothing().when(userService).deleteUserById(1);

        mockMvc.perform(delete("/api/users/1")
                        .with(user("1").roles(UserRole.ADMIN.toString()))
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    void deleteUserWithUserRole_thenForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(user("1").roles(UserRole.USER.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUserWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
