package com.customer.business.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.KafkaException;
//import org.springframework.web.bind.support.WebExchangeBindException;
//import org.springframework.validation.BindingResult;
import reactor.test.StepVerifier;

//import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleBusinessException retorna respuesta adecuada")
    void testHandleBusinessException() {
        BusinessException ex = new BusinessException(
                "msg", HttpStatus.BAD_REQUEST, "CODE");
        StepVerifier.create(handler.handleBusinessException(ex))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
                    Map<String, Object> body = resp.getBody();
                    assertEquals("CODE", body.get("code"));
                    assertEquals("msg", body.get("message"));
                })
                .verifyComplete();
    }

//    @Test
//    @DisplayName("handleValidationException retorna respuesta adecuada")
//    void testHandleValidationException() {
//        WebExchangeBindException ex = mock(WebExchangeBindException.class);
//        BindingResult bindingResult = mock(BindingResult.class);
//        when(ex.getMessage()).thenReturn("validation failed");
//        when(ex.getBindingResult()).thenReturn(bindingResult);
//        when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());
//        StepVerifier.create(handler.handleValidationException(ex))
//                .assertNext(resp -> {
//                    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
//                    Map<String, Object> body = resp.getBody();
//                    assertEquals("VALIDATION_ERROR", body.get("code"));
//                })
//                .verifyComplete();
//    }

    @Test
    @DisplayName("handleGenericException retorna respuesta adecuada")
    void testHandleGenericException() {
        Exception ex = new Exception("fail");
        StepVerifier.create(handler.handleGenericException(ex))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
                    Map<String, Object> body = resp.getBody();
                    assertEquals("INTERNAL_ERROR", body.get("code"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("handleIllegalArgumentException retorna respuesta adecuada")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");
        StepVerifier.create(handler.handleIllegalArgumentException(ex))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
                    Map<String, Object> body = resp.getBody();
                    assertEquals("ILLEGAL_ARGUMENT", body.get("code"));
                    assertEquals("bad arg", body.get("message"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("handleKafkaException retorna respuesta adecuada")
    void testHandleKafkaException() {
        KafkaException ex = new KafkaException("kafka fail");
        StepVerifier.create(handler.handleKafkaException(ex))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
                    Map<String, Object> body = resp.getBody();
                    assertEquals("KAFKA_ERROR", body.get("code"));
                })
                .verifyComplete();
    }
}
