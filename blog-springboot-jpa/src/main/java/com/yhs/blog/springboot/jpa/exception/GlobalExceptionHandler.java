package com.yhs.blog.springboot.jpa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> postCreationException(Exception ex) {
        return new ResponseEntity<>("Internal Server Error. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
