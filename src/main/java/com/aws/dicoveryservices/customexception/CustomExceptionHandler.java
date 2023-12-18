package com.aws.dicoveryservices.customexception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(value = CustomException.class)
    public ResponseEntity<ErrorResponse> notFound(Exception e){
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(),HttpStatus.NOT_FOUND.value(), e.getMessage())
                , HttpStatus.NOT_FOUND);
    }
}
