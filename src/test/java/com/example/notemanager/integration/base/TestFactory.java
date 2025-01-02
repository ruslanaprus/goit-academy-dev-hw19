package com.example.notemanager.integration.base;

import com.example.notemanager.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestFactory {

    @Autowired
    @Qualifier("passEncoder")
    private PasswordEncoder passwordEncoder;

    public User generateUser(String userName, String password, String role, int failedAttempts, LocalDateTime accountLockedUntil) {
        return User.builder()
                .userName(userName)
                .password(passwordEncoder.encode(password))
                .role(role)
                .failedAttempts(failedAttempts)
                .accountLockedUntil(accountLockedUntil)
                .build();
    }
}