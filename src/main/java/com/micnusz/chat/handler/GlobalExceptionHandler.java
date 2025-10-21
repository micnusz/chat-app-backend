package com.micnusz.chat.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.micnusz.chat.dto.ErrorResponseDTO;
import com.micnusz.chat.exception.AccessDeniedException;
import com.micnusz.chat.exception.IncorrectPasswordException;
import com.micnusz.chat.exception.RoomNotFoundException;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.exception.UserNotInRoomException;
import com.micnusz.chat.exception.UsernameAlreadyExistsException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponseDTO buildError(HttpStatus status, String message, String path) {
        return new ErrorResponseDTO(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameExists(UsernameAlreadyExistsException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(buildError(status, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgs(IllegalArgumentException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(buildError(status, "Invalid request.", req.getRequestURI()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(buildError(status, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleRoomNotFound(RoomNotFoundException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(buildError(status, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ErrorResponseDTO> handleIncorrectPassword(IncorrectPasswordException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(buildError(status, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(UserNotInRoomException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotInRoom(UserNotInRoomException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(buildError(status, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(buildError(status, ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(buildError(status, "Internal server error", req.getRequestURI()));
    }
}
