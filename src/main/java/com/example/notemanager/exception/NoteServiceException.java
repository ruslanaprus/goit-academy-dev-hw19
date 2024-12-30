package com.example.notemanager.exception;

public class NoteServiceException extends RuntimeException {
    public NoteServiceException(String message) {
        super(message);
    }
}
