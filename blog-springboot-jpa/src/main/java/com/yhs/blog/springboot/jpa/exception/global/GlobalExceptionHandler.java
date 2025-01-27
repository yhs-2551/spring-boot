package com.yhs.blog.springboot.jpa.exception.global;

import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException; 

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
 
import java.util.List;

import java.util.stream.Collectors;

// http 요청에 대한 예외 처리만 가능
@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                ex.getErrorCode().getStatus());

        // LoggingAspect에서 로깅 후 error를 던짐
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(error);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ErrorResponse> handleSystemException(SystemException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                ex.getErrorCode().getStatus());

        // LoggingAspect에서 로깅 후 error를 던짐
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(error);
    }

    // @PreAuthorize 어노테이션을 통해 권한이 없는 사용자가 접근할 때 발생하는 예외. 기본값이 500이기 때문에 403으로 변경
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ErrorResponse error = new ErrorResponse(
                "접근 권한이 없습니다.",
                HttpStatus.FORBIDDEN.value());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }
 
    // @Valid 어노테이션이 붙은 DTO에 유효성 검사를 통과하지 못하면 실행.
    // @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String errorMessage = String.join(", ", errorMessages);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generalException(Exception ex) {
        log.error("처리되지 않은 예외 발생: ", ex);

        ErrorResponse error = new ErrorResponse(
                "예기치 않은 서버 내부 오류가 발생했습니다.",
                HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.internalServerError().body(error);
    }

}
