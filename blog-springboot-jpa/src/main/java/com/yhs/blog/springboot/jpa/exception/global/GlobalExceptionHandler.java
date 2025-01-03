package com.yhs.blog.springboot.jpa.exception.global;

import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.exception.custom.ElasticsearchCustomException;
import com.yhs.blog.springboot.jpa.exception.custom.QueryDslCustomException;
import com.yhs.blog.springboot.jpa.exception.custom.ResourceNotFoundException;
import com.yhs.blog.springboot.jpa.exception.custom.UserCreationException;

import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    // @PreAuthorize 어노테이션을 통해 권한이 없는 사용자가 접근할 때 발생하는 예외. 기본값이 500이기 때문에 403으로 변경
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ErrorResponse error = new ErrorResponse(
                "You do not have permission to access.",
                HttpStatus.FORBIDDEN.value());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> findPostByIdException(ResourceNotFoundException ex) {

        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> runtimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // RuntimeException의 하위 클래스
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> IllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ElasticsearchCustomException.class)
    public ResponseEntity<ApiResponse> handleElasticsearchException(ElasticsearchCustomException ex) {
        log.error("Elasticsearch error: {}, code: {}", ex.getMessage(), ex.getErrorCode());

        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), Integer.parseInt(ex.getErrorCode()));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(QueryDslCustomException.class)
    public ResponseEntity<ApiResponse> queryDslCustomException(QueryDslCustomException ex) {
        log.error("QueryDsl error: {}, code: {}", ex.getMessage(), ex.getErrorCode());

        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), Integer.parseInt(ex.getErrorCode()));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(UserCreationException.class)
    public ResponseEntity<ErrorResponse> handleUserCreationException(UserCreationException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UsernameNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // @Valid 어노테이션이 붙은 DTO에 유효성 검사를 통과하지 못하면 실행.
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handlePostValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            erros.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> generalException(Exception ex) {
        return new ResponseEntity<>("Internal Server Error. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
