package com.example.notemanager;

import com.example.notemanager.api.model.dto.RestResponsePage;
import com.example.notemanager.api.model.dto.request.NoteCreateRequest;
import com.example.notemanager.api.model.dto.request.UserCreateRequest;
import com.example.notemanager.api.model.dto.request.UserLoginRequest;
import com.example.notemanager.api.model.dto.response.LoginResponse;
import com.example.notemanager.api.model.dto.response.NoteResponse;
import com.example.notemanager.api.model.dto.response.SignupResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
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
    void contextLoads() {
    }

    @Test
    void setPostgreSQLContainerIsRunning() {
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @Test
    void registerUserSuccessfullyAndAddANoteTest() {
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

        // 3. Check initial note count
        HttpEntity<Void> getNotesEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<RestResponsePage<NoteResponse>> notesResponse = restTemplate.exchange(
                SERVER_BASE_URL + port + API_BASE_URL + "/notes",
                HttpMethod.GET,
                getNotesEntity,
                new ParameterizedTypeReference<RestResponsePage<NoteResponse>>() {}
        );

        Page<NoteResponse> notes = notesResponse.getBody();
        assertThat(notes).isNotNull();
        assertThat(notes.getContent().size()).isEqualTo(0);

        // 4. Create a new note
        String title = "A New Note";
        String content = "Discussed project";
        NoteCreateRequest noteCreateRequest = new NoteCreateRequest(title, content);
        HttpEntity<NoteCreateRequest> createNoteEntity = new HttpEntity<>(noteCreateRequest, authHeaders);
        ResponseEntity<NoteResponse> createNoteResponse =
                restTemplate.postForEntity(SERVER_BASE_URL + port + API_BASE_URL + "/notes",
                        createNoteEntity, NoteResponse.class);

        assertThat(createNoteResponse.getStatusCode().value()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(createNoteResponse.getBody()).isNotNull();

        // 5: Verify note count increased
        ResponseEntity<RestResponsePage<NoteResponse>> updatedNotesResponse = restTemplate.exchange(
                SERVER_BASE_URL + port + API_BASE_URL + "/notes",
                HttpMethod.GET,
                getNotesEntity,
                new ParameterizedTypeReference<RestResponsePage<NoteResponse>>() {}
        );

        notes = updatedNotesResponse.getBody();
        assertThat(notes).isNotNull();
        assertThat(notes.getContent().size()).isEqualTo(1);

        // 6: Verify the created note data
        NoteResponse createdNote = notes.getContent().get(0);
        assertThat(createdNote.title()).isEqualTo(title);
        assertThat(createdNote.content()).isEqualTo(content);
    }
}
