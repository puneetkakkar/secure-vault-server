package com.securevault.main.security;

import java.io.IOException;
import java.util.Objects;

import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.securevault.main.exception.BadRequestException;
import com.securevault.main.exception.InvalidTokenException;
import com.securevault.main.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;

	public JwtAuthenticationFilter(
			@Lazy JwtTokenProvider jwtTokenProvider,
			@Lazy UserService userService) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userService = userService;
	}

	@Override
	protected final void doFilterInternal(@NonNull final HttpServletRequest request,
			@NonNull final HttpServletResponse response, @NonNull final FilterChain filterChain)
			throws ServletException, IOException {

		String token = jwtTokenProvider.extractJwtFromRequest(request);

		try {
			if (StringUtils.hasText(token)) {
				// Validate token and get validation result
				JwtTokenProvider.TokenValidationResult validationResult = jwtTokenProvider.validateToken(token);

				if (!validationResult.isValid()) {
					log.error("Token validation failed: {}", validationResult.getErrorMessage());
					throw new InvalidTokenException(validationResult.getErrorMessage());
				}

				// Get user ID from validated token
				String userId = jwtTokenProvider.getUserIdFromToken(token);

				// Load user details
				UserDetails user = userService.loadUserById(userId);

				if (Objects.nonNull(user) && SecurityContextHolder.getContext().getAuthentication() == null) {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
							user, null, user.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			}

			filterChain.doFilter(request, response);
		} catch (AccessDeniedException e) {
			throw new AccessDeniedException(e.getMessage());
		} catch (InvalidTokenException e) {
			log.error("Invalid token: {}", e.getMessage());
			request.setAttribute("access_denied", e.getMessage());
			throw new InvalidTokenException(e.getMessage());
		} catch (Exception e) {
			log.error("Error processing JWT token: {}", e.getMessage());
			request.setAttribute("access_denied", e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}
}
