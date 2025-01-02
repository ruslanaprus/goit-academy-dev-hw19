package com.example.notemanager.integration;

import com.example.notemanager.integration.base.BaseIT;
import com.example.notemanager.integration.base.TestFactory;
import com.example.notemanager.model.User;
import com.example.notemanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends BaseIT {
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 15;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    TestFactory testFactory;

    @Test
    @Transactional
    @DisplayName("Happy Path: User is successfully saved to the database")
    void givenUserEntity_whenSaveUser_thenUserIsPersisted(){
        // given
        User user = testFactory.generateUser("Alice", "qwerty", "ROLE_USER", 0, null);

        // when
        userRepository.save(user);
        Optional<User> retrievedUser = userRepository.findByUserName("Alice");

        // then
        assertTrue(retrievedUser.isPresent());
        assertEquals("Alice", retrievedUser.get().getUsername());
    }

    @Test
    @Transactional
    @DisplayName("Happy Path: Existing username check returns true")
    void givenExistingUser_whenExistsByUserName_thenReturnTrue() {
        // given
        User user = testFactory.generateUser("Alice", "qwerty", "ROLE_USER", 0, null);
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByUserName("Alice");

        // then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Non-existent username check returns false")
    void givenNonExistentUser_whenExistsByUserName_thenReturnFalse() {
        // when
        boolean exists = userRepository.existsByUserName("NonExistent");

        // then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Increment failed login attempts and lock user if needed")
    void givenUser_whenIncrementFailedAttempts_thenUpdateFailedAttemptsAndLockIfNeeded() {
        // given
        User user = testFactory.generateUser("Charlie", "mypassword", "ROLE_USER", 2, null);
        User savedUser = userRepository.save(user);

        // when
        LocalDateTime lockTime = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
        userRepository.incrementFailedAttempts(savedUser.getId(), MAX_FAILED_ATTEMPTS, lockTime);

        // then
        Optional<User> updatedUser = userRepository.findById(savedUser.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals(3, updatedUser.get().getFailedAttempts());
        assertEquals(lockTime, updatedUser.get().getAccountLockedUntil());
    }

    @Test
    @DisplayName("Reset failed login attempts for a user")
    void givenUser_whenResetFailedAttempts_thenResetValues() {
        // given
        User user = testFactory.generateUser("David","123456", "ROLE_USER", MAX_FAILED_ATTEMPTS, LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        User savedUser = userRepository.save(user);

        // when
        userRepository.resetFailedAttempts(savedUser.getId());

        // then
        Optional<User> updatedUser = userRepository.findById(savedUser.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals(0, updatedUser.get().getFailedAttempts());
        assertNull(updatedUser.get().getAccountLockedUntil());
    }
}