package com.example.notemanager.api.model.dto.response;

import lombok.Builder;

@Builder
public record ErrorResponse(int status,
                           String message) {
}