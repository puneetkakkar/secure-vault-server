package com.securevault.main.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.web.SecurityFilterChain;

import com.securevault.main.security.JwtAuthEntryPoint;
import com.securevault.main.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class WebSecurityConfiguration {
	private JwtAuthEntryPoint jwtAuthEntryPoint;
	// private UserService userService;

	// @Bean
	// public AuthenticationProvider authenticationProvider() {
	// DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	// authProvider.setUserDetailsService();
	// authProvider.setPasswordEncoder(passwordEncoder());
	// return authProvider;
	// }

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Pbkdf2PasswordEncoder("secret-key", 600000, 256, SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
	}

	@Bean
	public AuthenticationManager authenticationManager(UserService userService, PasswordEncoder encoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userService);
		provider.setPasswordEncoder(encoder);
		return new ProviderManager(provider);
	}

	// @Bean
	// public AuthenticationManager
	// authenticationManager(AuthenticationConfiguration authConfig) throws
	// Exception {
	// return authConfig.getAuthenticationManager();
	// }

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
		log.info("Security Filter Chain");
		return http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(
						requests -> requests.requestMatchers("/", "/dummy", "/api/auth/**").permitAll().anyRequest()
								.authenticated())
				.exceptionHandling(configurer -> configurer.authenticationEntryPoint(jwtAuthEntryPoint))
				.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// .authenticationProvider(authenticationProvider())
				.build();
	}

}
