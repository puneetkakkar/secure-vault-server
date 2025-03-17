package com.securevault.main.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securevault.main.enums.ErrorCode;
import com.securevault.main.enums.ResponseStatus;
import com.securevault.main.service.MessageSourceService;
import org.apache.coyote.BadRequestException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.securevault.main.dto.response.DetailedErrorResponse;
import com.securevault.main.dto.response.ErrorResponse;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class AppExceptionHandler {
    private final MessageSourceService messageSourceService;

    @ExceptionHandler(BindException.class)
    public final ResponseEntity<ErrorResponse> handleBindException(final BindException e) {
        log.error(e.toString(), e.getMessage());

        Map<String, List<Map<String, String>>> fieldErrors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            String message = error.getDefaultMessage();

            String value = handleRejectedValue(rejectedValue);

            Map<String, String> errorInfo = new HashMap<>();
            errorInfo.put("message", message);
            errorInfo.put("value", value != null ? value : "null");

            fieldErrors
                    .computeIfAbsent(fieldName, k -> new ArrayList<>())
                    .add(errorInfo);
        });

        return build(HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.UNPROCESSABLE_ENTITY, messageSourceService.get("validation_error"), fieldErrors);
    }

    @ExceptionHandler({
            BadRequestException.class,
            MultipartException.class,
            MissingServletRequestPartException.class,
            HttpMediaTypeNotSupportedException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class,
            InvalidDataAccessApiUsageException.class,
            ConstraintViolationException.class,
            MissingRequestHeaderException.class,
            MalformedJwtException.class
    })
    public final ResponseEntity<ErrorResponse> handleBadRequestException(final Exception e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
    }

    @ExceptionHandler({TokenExpiredException.class, RefreshTokenExpiredException.class})
    public final ResponseEntity<ErrorResponse> handleTokenExpiredRequestException(
            final TokenExpiredException e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
    }

    @ExceptionHandler({NotFoundException.class})
    public final ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({
            InternalAuthenticationServiceException.class,
            BadCredentialsException.class,
            AuthenticationCredentialsNotFoundException.class
    })
    public final ResponseEntity<ErrorResponse> handleBadCredentialsException(final Exception e) {
        log.error(e.toString(), e.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<ErrorResponse> handleAccessDeniedException(final Exception e) {
        return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, e.getMessage());
    }

    private String handleRejectedValue(Object rejectedValue) {
        if (rejectedValue == null) {
            return null;
        }

        if (rejectedValue instanceof String) {
            return (String) rejectedValue;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(rejectedValue);
        } catch (JsonProcessingException e) {
            log.error("Error serializing rejectedValue", e);
            return rejectedValue.toString();
        }
    }

    /**
     * Build error response
     *
     * @param httpStatus HttpStatus enum to response status field
     * @param message    String for response message field
     * @return ResponseEntity
     */
    private ResponseEntity<ErrorResponse> build(final HttpStatus httpStatus, final ErrorCode errorCode, final String message,
                                                final Map<String, List<Map<String, String>>> errors) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .build();

        DetailedErrorResponse detailedErrorResponse = DetailedErrorResponse.builder()
                .code(errorCode)
                .message(message)
                .timestamp(Instant.now())
                .errors(errors)
                .build();

        errorResponse.setStatus(ResponseStatus.ERROR.getValue());
        detailedErrorResponse.setStatus(ResponseStatus.ERROR.getValue());

        if (!errors.isEmpty()) {
            return ResponseEntity.status(httpStatus).body(detailedErrorResponse);
        }

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    /**
     * Build error response
     *
     * @param httpStatus HttpStatus enum to response status field
     * @param message    String for response message field
     * @return ResponsEntity
     */
    private ResponseEntity<ErrorResponse> build(final HttpStatus httpStatus, final ErrorCode errorCode, final String message) {
        Map<String, List<Map<String, String>>> errors = new HashMap<>();

        return build(httpStatus, errorCode, message, errors);
    }

}
