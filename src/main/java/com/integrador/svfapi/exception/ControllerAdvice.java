package com.integrador.svfapi.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDTO> handleException(BusinessException e) {
        ErrorDTO errorDTO = new ErrorDTO(e.getStatus().value(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleException(MethodArgumentNotValidException e) {
        ErrorDTO errorDTO = new ErrorDTO(e.getStatusCode().value(), "Invalid data format. Please, check the documentation");
        return ResponseEntity.status(400).body(errorDTO);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDTO> handleException(MissingServletRequestParameterException e) {
        ErrorDTO errorDTO = new ErrorDTO(400, "Invalid data format. Please, check the documentation");
        return ResponseEntity.status(400).body(errorDTO);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorDTO> handleException(MissingRequestHeaderException e) {
        ErrorDTO errorDTO = new ErrorDTO(400, "Invalid data format. Please, check the documentation");
        return ResponseEntity.status(400).body(errorDTO);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleException(Exception e) {
        ErrorDTO errorDTO = new ErrorDTO(500, "Internal server error"+e.getMessage());
        return ResponseEntity.status(500).body(errorDTO);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorDTO> handleException(NoHandlerFoundException e) {
        ErrorDTO errorDTO = new ErrorDTO(e.getStatusCode().value(), "Invalid endpoint. Please, check the documentation");
        return ResponseEntity.status(404).body(errorDTO);
    }


}
