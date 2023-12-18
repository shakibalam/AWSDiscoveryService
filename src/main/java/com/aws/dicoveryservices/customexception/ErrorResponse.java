package com.aws.dicoveryservices.customexception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime dateTime;
    private int status;
    private String msg;
}
