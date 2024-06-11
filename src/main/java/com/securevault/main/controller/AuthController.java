package com.securevault.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.main.configuration.api.ApiVersion;
import com.securevault.main.dto.request.auth.LoginRequest;
import com.securevault.main.dto.request.auth.RegisterRequest;
import com.securevault.main.dto.response.SuccessResponse;
import com.securevault.main.dto.response.auth.TokenResponse;
import com.securevault.main.service.AuthService;
import com.securevault.main.service.MessageSourceService;
import com.securevault.main.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@ApiVersion("1")
@RequiredArgsConstructor
public class AuthController extends AbstractBaseController {
	private final AuthService authService;
	private final UserService userService;
	private final MessageSourceService messageSourceService;

	@PostMapping("/register")
	public ResponseEntity<SuccessResponse> register(@RequestBody @Valid RegisterRequest request) throws BindException {
		userService.register(request);

		return ResponseEntity
				.ok(SuccessResponse.builder().message(messageSourceService.get("registered_successfully")).build());
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@RequestBody @Validated final LoginRequest request) {

		return ResponseEntity
				.ok(authService.login(request.getEmail(), request.getMasterPasswordHash(), request.getRememberMe()));
	}

	@GetMapping("/refresh")
	public ResponseEntity<TokenResponse> refresh(@CookieValue("refreshToken") @Validated final String refreshToken) {
		return ResponseEntity.ok(authService.refreshFromCookie(refreshToken));
	}

	@GetMapping("/logout")
	public ResponseEntity<SuccessResponse> logout() {
		authService.logout(userService.getUser());

		return ResponseEntity
				.ok(SuccessResponse.builder().message(messageSourceService.get("logout_successfully")).build());
	}

	@GetMapping("/dummy")
	public String hello1(@RequestParam(value = "name", defaultValue = "Java") String name) {
		return String.format("Yay! Hello %s V1!", name);
	}

	@GetMapping("/dummy")
	@ApiVersion("2")
	public String hello2(@RequestParam(value = "name", defaultValue = "Java") String name) {
		return String.format("Yay! Hello %s V2!", name);
	}

}
