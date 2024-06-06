package com.securevault.main.security;

import java.io.IOException;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.securevault.main.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;

	private final UserService userService;

	private final AuthenticationManager authenticationManager;

	@Override
	protected final void doFilterInternal(@NonNull final HttpServletRequest request,
			@NonNull final HttpServletResponse response, @NonNull final FilterChain filterChain)
			throws ServletException, IOException {

		if (jwtTokenProvider != null) {
			String token = jwtTokenProvider.extractJwtFromRequest(request);

			/**
			 * Performing authentication of the user based on the
			 * information extracted from the token provided.
			 */
			if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token, request)) {
				String id = jwtTokenProvider.getUserIdFromToken(token);
				UserDetails user = userService.loadUserById(id);

				if (Objects.nonNull(user)) {
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
							user.getAuthorities());
					authenticationManager.authenticate(auth);
				}
			}
		}

		filterChain.doFilter(request, response);

		log.info(request.getRemoteAddr());
	}

}
