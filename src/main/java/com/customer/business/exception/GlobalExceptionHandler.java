package com.customer.business.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", ex.getStatus().value());
        response.put("error", ex.getStatus().getReasonPhrase());
        response.put("code", ex.getCode());
        response.put("message", ex.getMessage());
        
        return Mono.just(ResponseEntity.status(ex.getStatus()).body(response));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(
            WebExchangeBindException ex) {
        log.warn("Validation exception: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "Validation failed for request");
        response.put("details", ex.getBindingResult().getAllErrors());
        
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.put("code", "INTERNAL_ERROR");
        response.put("message", "An unexpected error occurred");
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("Illegal argument exception: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        response.put("code", "ILLEGAL_ARGUMENT");
        response.put("message", ex.getMessage());
        
        return Mono.just(ResponseEntity.badRequest().body(response));
    }

    @ExceptionHandler(KafkaException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleKafkaException(KafkaException ex) {
        log.error("Kafka exception: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        response.put("code", "KAFKA_ERROR");
        response.put("message", "Message broker unavailable");

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}