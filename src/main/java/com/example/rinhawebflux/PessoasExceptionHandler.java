package com.example.rinhawebflux;

import org.springframework.core.codec.CodecException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ServerWebInputException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class PessoasExceptionHandler {
    @ExceptionHandler(DateTimeParseException.class)
    ResponseEntity<?> dateTimeParseException(DateTimeParseException error) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(ServerWebInputException.class)
    ResponseEntity<?> serverWebInputException(ServerWebInputException error) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(MissingRequestValueException.class)
    ResponseEntity<?> missingRequestValueException(MissingRequestValueException error) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(CodecException.class)
    ResponseEntity<?> codecException(CodecException error) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> anyException(Exception error) {
        error.printStackTrace();
        return ResponseEntity.badRequest().build();
    }
}
