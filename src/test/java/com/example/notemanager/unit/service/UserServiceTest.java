package com.example.notemanager.unit.service;

import com.example.notemanager.exception.EntityException;
import com.example.notemanager.exception.ExceptionMessages;
import com.example.notemanager.model.User;
import com.example.notemanager.repository.UserRepository;
import com.example.notemanager.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Cache<String, User> userCache;

    @Test
    @DisplayName("Happy Path Test: save new user and return 'User created'")
    void givenNewUsername_whenCreateUser_thenReturnUserCreated() {
        // given
        String username = "newUser";
        String password = "securePassword";
        String encodedPassword = "encodedPassword";

        when(userRepository.existsByUserName(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // when
        String result = userService.createUser(username, password);

        // then
        verify(userRepository, times(1)).existsByUserName(username);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(argThat(user ->
                user.getUsername().equals(username) &&
                        user.getPassword().equals(encodedPassword) &&
                        user.getRole().equals("ROLE_USER")
        ));
        assertEquals("User created", result);
    }

    @Test
    @DisplayName("Should return 'User already exists' when user with username exists")
    void givenExistingUsername_whenCreateUser_thenReturnUserAlreadyExists() {
        // given
        String username = "existingUser";
        String password = "securePassword";
        when(userRepository.existsByUserName(username)).thenReturn(true);

        // when
        String result = userService.createUser(username, password);

        // then
        verify(userRepository, times(1)).existsByUserName(username);
        verify(userRepository, never()).save(any(User.class));
        assertEquals("User already exists", result);
    }

    @Test
    @DisplayName("Happy Path Test: Find user by username")
    void givenValidUserName_whenFindByUserName_thenReturnUser() {
        // given
        String mockUserName = "testUser";
        User mockUser = User.builder().userName(mockUserName).password("password").role("ROLE_USER").build();
        doReturn(Optional.of(mockUser)).when(userRepository).findByUserName(mockUserName);

        // when
        Optional<User> user = userService.findByUserName(mockUserName);

        // then
        verify(userRepository, times(1)).findByUserName(mockUserName);
        assertTrue(user.isPresent());
        assertEquals(mockUserName, user.get().getUsername());
    }

    @Test
    @DisplayName("Exception Test: User not found by username")
    void givenInvalidUserName_whenFindByUserName_thenThrowEntityException() {
        // given
        String invalidUserName = "nonExistentUser";
        doReturn(Optional.empty()).when(userRepository).findByUserName(invalidUserName);

        // when & then
        EntityException exception = assertThrows(EntityException.class, () -> {
            userService.findByUserName(invalidUserName);
        });

        verify(userRepository, times(1)).findByUserName(invalidUserName);
        assertEquals(ExceptionMessages.ENTITY_NOT_FOUND.getMessage(), exception.getMessage());
    }
}