package com.example.notemanager.integration.util;

import com.example.notemanager.api.model.dto.request.UserCreateRequest;

public class TestUtil {

    public static UserCreateRequest createUserRequest(String userName, String password) {
        return UserCreateRequest.builder()
                .userName(userName)
                .password(password)
                .build();
    }
}