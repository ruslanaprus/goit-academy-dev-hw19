package com.example.notemanager;

import com.example.notemanager.api.model.dto.request.NoteCreateRequest;
import com.example.notemanager.api.model.dto.request.UserCreateRequest;
import com.example.notemanager.api.model.dto.request.UserLoginRequest;
import com.example.notemanager.api.model.dto.response.LoginResponse;
import com.example.notemanager.api.model.dto.response.NoteResponse;
import com.example.notemanager.api.model.dto.response.SignupResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.example.notemanager.config.TestcontainersConfig.postgreSQLContainer;
import static com.example.notemanager.util.TestUtil.createUserRequest;
import static org.assertj.core.api.Assertions.assertThat;

class UserLifecycleIT extends BaseIT {

    @Test
    void contextLoads() {}

    @Test
    void setPostgreSQLContainerIsRunning(){
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @Test
    void registerUserSuccessfullyTest() throws JsonProcessingException {
        // 1. user registration
        String userName = "bob";
        String password = "password";
        UserCreateRequest userCreateRequest = createUserRequest(userName, password);
        ResponseEntity<SignupResponse> signupResponse =
                restTemplate.postForEntity(SERVER_BASE_URL + port + API_BASE_URL + "/signup",
                        userCreateRequest, SignupResponse.class);

        assertThat(signupResponse.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

        // 2. User login
        UserLoginRequest userLoginRequest = new UserLoginRequest(userName, password);
        ResponseEntity<LoginResponse> loginResponse =
                restTemplate.postForEntity(SERVER_BASE_URL + port + API_BASE_URL + "/login",
                        userLoginRequest, LoginResponse.class);

        assertThat(loginResponse.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        assertThat(loginResponse.getBody()).isNotNull();

        String jwt = loginResponse.getBody().token();

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(jwt);

        // 3. Create a new note
        String title = "A New Note";
        String content = "Discussed project";
        NoteCreateRequest noteCreateRequest = new NoteCreateRequest(title, content);
        HttpEntity<NoteCreateRequest> createNoteEntity = new HttpEntity<>(noteCreateRequest, authHeaders);
        ResponseEntity<NoteResponse> createNoteResponse =
                restTemplate.postForEntity(SERVER_BASE_URL + port + API_BASE_URL + "/notes",
                        createNoteEntity, NoteResponse.class);

        assertThat(createNoteResponse.getStatusCode().value()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(createNoteResponse.getBody()).isNotNull();

        // Step 4: Fetch the note by ID
        String createdNoteId = "20";
        HttpEntity<Void> getNoteEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<NoteResponse> getNoteResponse =
                restTemplate.exchange(SERVER_BASE_URL + port + API_BASE_URL + "/notes/" + createdNoteId,
                        HttpMethod.GET, getNoteEntity, NoteResponse.class);

        assertThat(getNoteResponse.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        assertThat(getNoteResponse.getBody()).isNotNull();
        assertThat(getNoteResponse.getBody().title()).isEqualTo(noteCreateRequest.title());
        assertThat(getNoteResponse.getBody().content()).isEqualTo(noteCreateRequest.content());
    }
}
