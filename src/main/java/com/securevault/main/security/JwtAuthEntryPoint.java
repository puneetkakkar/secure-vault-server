package com.securevault.main.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securevault.main.dto.response.ErrorResponse;
import com.securevault.main.exception.AppExceptionHandler;
import com.securevault.main.service.MessageSourceService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    private final MessageSourceService messageSourceService;

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        final String expired = (String) request.getAttribute("expired");
        final String unsupported = (String) request.getAttribute("unsupported");
        final String invalid = (String) request.getAttribute("invalid");
        final String illegal = (String) request.getAttribute("illegal");
        final String notfound = (String) request.getAttribute("notfound");
        final String message;

        if (expired != null) {
            message = expired;
        } else if (unsupported != null) {
            message = unsupported;
        } else if (invalid != null) {
            message = invalid;
        } else if (illegal != null) {
            message = illegal;
        } else if (notfound != null) {
            message = notfound;
        } else {
            message = authException.getMessage();
        }

        log.error("Could not set user authentication in security context. Error: {}", message);

        ResponseEntity<ErrorResponse> responseEntity = new AppExceptionHandler()
                .handleBadCredentialsException(new BadCredentialsException(message));

        response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

}
