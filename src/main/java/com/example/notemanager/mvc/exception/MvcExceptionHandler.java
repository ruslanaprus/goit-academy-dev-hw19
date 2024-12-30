package com.example.notemanager.mvc.exception;

import com.example.notemanager.exception.EntityException;
import com.example.notemanager.exception.NoteServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = "com.example.notemanager.mvc")
public class MvcExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(MvcExceptionHandler.class);

    @ExceptionHandler({NoteServiceException.class, EntityException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDomainSpecificExceptions(Exception exception, Model model) {
        log.error("A domain-specific issue was detected: {}", exception.getMessage(), exception);
        model.addAttribute("message", exception.getMessage());
        return "note/error";
    }

    @ExceptionHandler(value = Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Throwable exception, Model model) {
        log.error("Unexpected error occurred: {}", exception.getMessage(), exception);
        model.addAttribute("message", "Internal Server Error");
        return "note/error";
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAuthenticationException(AuthenticationException exception, Model model) {
        log.error("Authentication failed: {}", exception.getMessage());
        model.addAttribute("message", "Invalid username or password");
        return "login";
    }

}
