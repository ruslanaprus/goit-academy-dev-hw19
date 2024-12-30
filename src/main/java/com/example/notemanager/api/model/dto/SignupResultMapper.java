package com.example.notemanager.api.model.dto;

import com.example.notemanager.api.model.dto.response.SignupResponse;
import org.springframework.stereotype.Component;

@Component
public class SignupResultMapper {
    public SignupResponse toResponse(String userName, String message) {
        return SignupResponse.builder()
                .userName(userName)
                .message(message)
                .build();
    }
}
