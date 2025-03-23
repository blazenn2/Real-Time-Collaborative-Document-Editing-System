package com.blazenn.realtime_document_editing.controller.advice;

import com.blazenn.realtime_document_editing.controller.advice.errors.InvalidPasswordException;
import com.blazenn.realtime_document_editing.controller.advice.errors.UserNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(UserNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.create(ex, HttpStatusCode.valueOf(404), ex.getMessage());
        return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse.getBody());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPasswordException(InvalidPasswordException ex) {
        ErrorResponse errorResponse = ErrorResponse.create(ex, HttpStatusCode.valueOf(401), ex.getMessage());
        return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse.getBody());
    }
}
