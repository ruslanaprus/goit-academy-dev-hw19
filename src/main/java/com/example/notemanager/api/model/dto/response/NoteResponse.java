package com.example.notemanager.api.model.dto.response;

import lombok.Builder;

@Builder
public record NoteResponse(String title,
                           String content) {
}