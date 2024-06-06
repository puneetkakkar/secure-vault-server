package com.securevault.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.main.dto.request.auth.LoginRequest;
import com.securevault.main.dto.request.auth.RegisterRequest;
import com.securevault.main.dto.response.SuccessResponse;
import com.securevault.main.dto.response.auth.TokenResponse;
import com.securevault.main.service.AuthService;
import com.securevault.main.service.MessageSourceService;
import com.securevault.main.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
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

}
