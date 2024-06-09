package com.securevault.main.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.securevault.main.security.JwtAuthEntryPoint;
import com.securevault.main.security.JwtAuthenticationFilter;
import com.securevault.main.service.UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfiguration {
	@Value("${app.pbkdf2.secret}")
	private String pbkdf2Secret;

	@Value("${app.pbkdf2.salt-length}")
	private Integer pbkdf2SaltLength;

	@Value("${app.pbkdf2.iterations}")
	private Integer pbkdf2Iterations;

	private final JwtAuthEntryPoint jwtAuthEntryPoint;

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder(pbkdf2Secret, pbkdf2SaltLength, pbkdf2Iterations,
				SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
	}

	@Bean
	public AuthenticationManager authenticationManager(UserService userService,
			PasswordEncoder encoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userService);
		provider.setPasswordEncoder(encoder);
		return new ProviderManager(provider);
	}

	/**
	 * Configure Spring Security
	 *
	 * @param http
	 * @return SecurityFilterChain
	 * @throws Exception
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.exceptionHandling(configurer -> configurer.authenticationEntryPoint(jwtAuthEntryPoint))
				.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(configurer -> configurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(
						requests -> requests.requestMatchers("/", "/api/v1/auth/**").permitAll().anyRequest()
								.authenticated())
				.build();
	}

}
