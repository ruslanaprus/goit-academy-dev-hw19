package com.example.notemanager.unit.api.controller;

import com.example.notemanager.api.config.ApiSecurityConfig;
import com.example.notemanager.api.controller.AuthApiController;
import com.example.notemanager.api.model.dto.SignupResultMapper;
import com.example.notemanager.api.model.dto.request.UserCreateRequest;
import com.example.notemanager.api.model.dto.response.LoginResponse;
import com.example.notemanager.api.model.dto.response.SignupResponse;
import com.example.notemanager.api.util.JwtUtil;
import com.example.notemanager.model.User;
import com.example.notemanager.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthApiController.class)
@Import(ApiSecurityConfig.class)
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private SignupResultMapper signupResultMapper;

    @MockBean
    private @Qualifier("passEncoder") PasswordEncoder passwordEncoder;

    @MockBean
    private Cache<String, User> userCache;

    @BeforeEach
    void setup() {
        Mockito.reset(userService, jwtUtil, signupResultMapper, passwordEncoder, userCache);

        // mock password encoding behavior
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> {
            String rawPassword = invocation.getArgument(0);
            return "$2a$10$" + rawPassword; // example encoded password stub
        });

        // mock password matching behavior
        when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(invocation -> {
            String rawPassword = invocation.getArgument(0);
            String encodedPassword = invocation.getArgument(1);
            return encodedPassword.equals("$2a$10$" + rawPassword); // simulate bcrypt matching
        });
    }

    @Test
    @DisplayName("Happy Path: User signup")
    void givenValidRequest_whenSignup_thenReturnSignupResponse() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("Whiskers", "youshallnotpass");
        SignupResponse response = new SignupResponse("Whiskers", "User created");

        when(userService.createUser(request.userName(), request.password())).thenReturn("User created");
        when(signupResultMapper.toResponse(request.userName(), "User created")).thenReturn(response);

        // when
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"Whiskers\", \"password\":\"youshallnotpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Whiskers"))
                .andExpect(jsonPath("$.message").value("User created"));

        // then
        verify(userService, times(1)).createUser(request.userName(), request.password());
    }

    @Test
    @DisplayName("Error Case: User signup failure")
    void givenExceptionDuringSignup_whenSignup_thenReturnErrorResponse() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("Whiskers", "password123");
        String errorMessage = "Failed to create user";

        // simulating the exception thrown by the userService.createUser()
        when(userService.createUser(request.userName(), request.password()))
                .thenThrow(new RuntimeException());

        // simulating the response from the signupResultMapper for error case
        when(signupResultMapper.toResponse(null, errorMessage))
                .thenReturn(new SignupResponse(null, errorMessage));

        // when
        mockMvc.perform(post("/api/v1/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"Whiskers\", \"password\":\"password123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(errorMessage));

        // then
        verify(userService, times(1)).createUser(request.userName(), request.password());
    }

    @Test
    @DisplayName("Happy Path: User login success")
    void givenValidRequest_whenLogin_thenReturnToken() throws Exception {
        // given
        String username = "Whiskers";
        String password = "youshallnotpass";

        User user = new User(1L, username, passwordEncoder.encode(password), "USER", 0, null, null);

        LoginResponse response = new LoginResponse("meowstsecureandverylongandundecipherabletoken");

        when(userService.findByUserName(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn(response.token());

        // when
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"Whiskers\", \"password\":\"youshallnotpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.token()));

        // then
        verify(userService, times(1)).findByUserName(username);
        verify(passwordEncoder, times(1)).matches(password, user.getPassword());
        verify(jwtUtil, times(1)).generateToken(user);
    }

    @Test
    @DisplayName("Error Case: User not found during login")
    void givenInvalidUser_whenLogin_thenReturnUnauthorized() throws Exception {
        // given
        String username = "Whiskers";

        when(userService.findByUserName(username)).thenReturn(Optional.empty());

        // when
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"Whiskers\", \"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        // then
        verify(userService, times(1)).findByUserName(username);
    }

    @Test
    @DisplayName("Error Case: Invalid password during login")
    void givenInvalidPassword_whenLogin_thenReturnUnauthorized() throws Exception {
        // given
        String username = "Whiskers";
        String password = "wrongpassword";
        User user = new User(1L, username, passwordEncoder.encode(password), "USER", 0, null, null);

        when(userService.findByUserName(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        // when
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"Whiskers\", \"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        // then
        verify(userService, times(1)).findByUserName(username);
        verify(passwordEncoder, times(1)).matches(password, user.getPassword());
    }

    @Test
    @DisplayName("Error Case: Account locked during login")
    void givenLockedAccount_whenLogin_thenReturnLocked() throws Exception {
        // given
        String username = "Whiskers";
        String encodedPassword = passwordEncoder.encode("youshallnotpass");

        User user = new User(1L, username, encodedPassword, "USER", 0, LocalDateTime.now().plusMinutes(15), null);

        when(userService.findByUserName(username)).thenReturn(Optional.of(user));
        when(userService.isAccountLocked(user)).thenReturn(true);

        // when
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"Whiskers\", \"password\":\"youshallnotpass\"}"))
                .andExpect(status().isLocked())  // This will now check for 423 status
                .andExpect(jsonPath("$.message").value("User is locked. Try again later."));

        // then
        verify(userService, times(1)).findByUserName(username);
    }
}