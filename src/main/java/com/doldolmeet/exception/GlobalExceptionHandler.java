package com.doldolmeet.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // CustomException 클래스에서 발생하는 예외 핸들러
    @ExceptionHandler(value = {CustomException.class})
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    // Valid 예외 핸들러
    @ExceptionHandler(value = {BindException.class})
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        BindingResult bindingResult = ex.getBindingResult();

        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getDefaultMessage());
        }
        return ErrorResponse.toResponseEntityValid(sb.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();
        log.error("===== Exception 메세지 확인 : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        log.error("===== RuntimeException 메세지 확인 : {}", e.getMessage());
        return ErrorResponse.toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
    }
}
