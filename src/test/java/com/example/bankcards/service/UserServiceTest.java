package com.example.bankcards.service;

import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.dto.UserUpdateRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest extends BaseServiceTest {

    @InjectMocks
    private UserService userService;

    private UserCreateRequest testUserCreateRequest() {
        return new UserCreateRequest("Ivan", "ivan@example.com", "password", UserRole.ADMIN);
    }

    private UserUpdateRequest testUserUpdateRequest() {
        return new UserUpdateRequest("new@example.com", "password", UserStatus.ACTIVE);
    }

    private UserResponse testUserResponse() {
        return UserResponse.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@example.com")
                .build();
    }

    // createUser
    @Test
    void createUser_shouldReturnUserResponse() {
        UserCreateRequest request = testUserCreateRequest();

        User user = testUser();
        UserResponse response = testUserResponse();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(testUserResponse());

        UserResponse result = userService.createUser(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Ivan");
        assertThat(result.getEmail()).isEqualTo("ivan@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void createUser_whenEmailAlreadyExists_shouldThrowConflictException() {
        UserCreateRequest request = testUserCreateRequest();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser()));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("User with this email already exists");

        verify(userRepository).findByEmail(request.getEmail());
    }

    // getAllUsers
    @Test
    void getAllUsers_WithPagination_shouldReturnPageOfUsersResponse() {
        Pageable pageable = PageRequest.of(0, 10);

        User user = testUser();
        UserResponse response = testUserResponse();

        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponse(user)).thenReturn(response);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
        assertThat(result).isNotNull();

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userMapper).toUserResponse(user);
    }

    // getUserById
    @Test
    void getUserById_shouldReturnUserResponse() {
        User user = testUser();
        Optional<User> optionalUser = Optional.of(user);
        UserResponse response = testUserResponse();

        when(userRepository.findById(1L)).thenReturn(optionalUser);
        when(userMapper.toUserResponse(user)).thenReturn(response);

        UserResponse result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Ivan");
        assertThat(result.getEmail()).isEqualTo("ivan@example.com");

        verify(userRepository).findById(1L);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void getUserById_whenUserNotFound_shouldThrowNotFoundException() {
        long nonExistingUserId = 9999L;

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(nonExistingUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not found user with id: " + nonExistingUserId);

        verify(userRepository).findById(nonExistingUserId);
    }

    // updateUser
    @Test
    void updateUser_shouldReturnUserResponse() {
        User user = User.builder()
                .id(1L)
                .name("Ivan")
                .email("new@example.com")
                .password("encodedPassword")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .cards(List.of())
                .build();
        UserUpdateRequest request = testUserUpdateRequest();
        UserResponse response = new UserResponse(1L, "Ivan", "new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(response);

        UserResponse result = userService.updateUser(1L, request);

        assertThat(result.getEmail()).isEqualTo("new@example.com");

        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowNotFoundException() {
        long nonExistingUserId = 9999L;

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(nonExistingUserId, testUserUpdateRequest()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not found user with id: " + nonExistingUserId);

        verify(userRepository).findById(nonExistingUserId);
    }

    // deleteUserById
    @Test
    void deleteUserById_shouldCallRepositoryDelete() {
        User user = testUser();
        Long id = user.getId();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(id);

        assertThatNoException().isThrownBy(() -> userService.deleteUserById(id));

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteUserById_whenUserNotFound_shouldThrowNotFoundException() {
        long nonExistingUserId = 9999L;

        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(nonExistingUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not found user with id: " + nonExistingUserId);

        verify(userRepository, times(1)).findById(nonExistingUserId);
    }
}
