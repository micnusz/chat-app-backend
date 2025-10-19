package com.micnusz.chat.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.micnusz.chat.dto.ErrorResponseDTO;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.exception.UsernameAlreadyExistsException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameExists(UsernameAlreadyExistsException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgs(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO("Invalid request."));
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(exception.getMessage()));
    }


}
