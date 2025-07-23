package com.example.bankcards.service;


import com.example.bankcards.dto.UserCreateRequest;
import com.example.bankcards.dto.UserUpdateRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("User with this email already exists");
        }

        User user = userMapper.toEntity(request);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {

        Specification<User> specification = (root, query, cb) -> null;

        return userRepository.findAll(specification, pageable).map(userMapper::toUserResponse);
    }

    public UserResponse getUserById(long id) {
        final User user = userRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Not found user with id: " + id));

        return userMapper.toUserResponse(user);
    }

    public void deleteUserById(long id) {
        if (userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new NotFoundException("Not found user with id: " + id);
        }
    }

    public UserResponse updateUser(long id, UserUpdateRequest request) {
        User user = userRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Not found user with id: " + id));

        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findById(Long.parseLong(username))
                             .orElseThrow(() -> new UsernameNotFoundException("Not found user with id: " + username));
    }

    public Optional<User> findById(final Long userId) {
        return userRepository.findById(userId);
    }
}
