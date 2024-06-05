package com.securevault.main.exception;

import java.util.HashMap;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@NoArgsConstructor
@Slf4j
public class AppExceptionHandler {

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
		return build(HttpStatus.BAD_REQUEST, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
	}

	@ExceptionHandler({ NotFoundException.class })
	public final ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException e) {
		log.error(e.toString(), e.getMessage());
		return build(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler({
			InternalAuthenticationServiceException.class,
			BadCredentialsException.class,
	})
	public final ResponseEntity<ErrorResponse> handleBadCredentialsException(final Exception e) {
		log.error(e.toString(), e.getMessage());
		return build(HttpStatus.UNAUTHORIZED, e.getMessage());
	}

	/**
	 * Build error response
	 * 
	 * @param httpStatus HttpStatus enum to response status field
	 * @param message    String for response message field
	 * @return ResponsEntity
	 */
	private ResponseEntity<ErrorResponse> build(final HttpStatus httpStatus, final String message,
			final Map<String, String> errors) {

		if (!errors.isEmpty()) {
			return ResponseEntity.status(httpStatus)
					.body(DetailedErrorResponse.builder().message(message).items(errors).build());
		}

		return ResponseEntity.status(httpStatus).body(ErrorResponse.builder().message(message).build());
	}

	/**
	 * Build error response
	 * 
	 * @param httpStatus HttpStatus enum to response status field
	 * @param message    String for response message field
	 * @return ResponsEntity
	 */
	private ResponseEntity<ErrorResponse> build(final HttpStatus httpStatus, final String message) {
		return build(httpStatus, message, new HashMap<>());
	}

}
