package com.example.notemanager.api.model.dto.response;

import lombok.Builder;

@Builder
public record SignupResponse(String userName,
                             String message) {
}
