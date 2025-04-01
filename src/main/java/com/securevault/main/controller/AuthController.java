package com.securevault.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.main.configuration.api.ApiVersion;
import com.securevault.main.dto.request.auth.LoginRequest;
import com.securevault.main.dto.request.auth.RegisterRequest;
import com.securevault.main.dto.request.auth.SendEmailVerificationRequest;
import com.securevault.main.dto.response.SuccessResponse;
import com.securevault.main.dto.response.auth.TokenResponse;
import com.securevault.main.enums.ResponseStatus;
import com.securevault.main.exception.BadRequestException;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.exception.EmailSendingException;
import com.securevault.main.service.AuthService;
import com.securevault.main.service.EmailVerificationService;
import com.securevault.main.service.MessageSourceService;
import com.securevault.main.service.UserService;
import com.securevault.main.util.ApiEndpoints;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(ApiEndpoints.AUTH_BASE_URL)
@ApiVersion("1")
@RequiredArgsConstructor
public class AuthController extends AbstractBaseController {
	private final AuthService authService;
	private final UserService userService;
	private final MessageSourceService messageSourceService;
	private final EmailVerificationService emailVerificationService;

	@PostMapping(ApiEndpoints.AUTH_SEND_EMAIL_VERIFICATION_URL)
	public ResponseEntity<SuccessResponse> sendEmailVerification(
			@RequestBody @Valid SendEmailVerificationRequest request) {
		try {
			emailVerificationService.sendEmailVerification(request);
			return ResponseEntity.ok(SuccessResponse.builder()
					.status(ResponseStatus.SUCCESS.getValue())
					.message(messageSourceService.get("email_verification_sent"))
					.build());
		} catch (BadRequestException e) {
			log.error("Bad request during email verification send: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("email_verification_failed"));
		} catch (NotFoundException e) {
			log.error("User not found during email verification send: {}", e.getMessage());
			throw new NotFoundException(messageSourceService.get("user_not_found"));
		} catch (EmailSendingException e) {
			log.error("Failed to send verification email: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("email_verification_failed"));
		} catch (Exception e) {
			log.error("Unexpected error during email verification send: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("service_unavailable"));
		}
	}

	@GetMapping(ApiEndpoints.AUTH_EMAIL_VERIFICATION_URL)
	public ResponseEntity<SuccessResponse> verifyEmail(@PathVariable String token) {
		try {
			emailVerificationService.verifyEmail(token);
			return ResponseEntity.ok(SuccessResponse.builder()
					.status(ResponseStatus.SUCCESS.getValue())
					.message(messageSourceService.get("email_verified"))
					.build());
		} catch (BadRequestException e) {
			log.error("Bad request during email verification: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("invalid_verification"));
		} catch (NotFoundException e) {
			log.error("Token not found during email verification: {}", e.getMessage());
			throw new NotFoundException(messageSourceService.get("verification_link_invalid"));
		} catch (Exception e) {
			log.error("Unexpected error during email verification: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("service_unavailable"));
		}
	}

	@PostMapping(ApiEndpoints.AUTH_REGISTER_URL)
	public ResponseEntity<SuccessResponse> register(@RequestBody @Valid RegisterRequest request) throws BindException {
		userService.register(request);

		return ResponseEntity
				.ok(SuccessResponse.builder().status(ResponseStatus.SUCCESS.getValue())
						.message(messageSourceService.get("registered_successfully")).build());
	}

	@PostMapping(ApiEndpoints.AUTH_LOGIN_URL)
	public ResponseEntity<TokenResponse> login(@RequestBody @Validated final LoginRequest request) {

		TokenResponse loginTokenResponse = authService.login(request.getEmail(), request.getMasterPasswordHash(),
				request.getRememberMe());
		loginTokenResponse.setStatus(ResponseStatus.SUCCESS.getValue());

		return ResponseEntity.ok(loginTokenResponse);
	}

	@GetMapping(ApiEndpoints.AUTH_REFRESH_TOKEN_URL)
	public ResponseEntity<TokenResponse> refresh(@CookieValue("refreshToken") @Validated final String refreshToken) {
		TokenResponse refreshTokenResponse = authService.refreshFromCookie(refreshToken);
		refreshTokenResponse.setStatus(ResponseStatus.SUCCESS.getValue());

		return ResponseEntity.ok(refreshTokenResponse);
	}

	@GetMapping(ApiEndpoints.AUTH_LOGOUT_URL)
	public ResponseEntity<SuccessResponse> logout() {
		authService.logout(userService.getUser());

		return ResponseEntity
				.ok(SuccessResponse.builder().status(ResponseStatus.SUCCESS.getValue())
						.message(messageSourceService.get("logout_successfully")).build());
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
