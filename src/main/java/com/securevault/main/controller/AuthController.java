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
import com.securevault.main.dto.request.auth.FinishRegistrationRequest;
import com.securevault.main.dto.request.auth.LoginRequest;
import com.securevault.main.dto.request.auth.SendEmailVerificationRequest;
import com.securevault.main.dto.response.SuccessResponse;
import com.securevault.main.dto.response.auth.TokenResponse;
import com.securevault.main.exception.AccountLockedException;
import com.securevault.main.exception.BadRequestException;
import com.securevault.main.exception.EmailSendingException;
import com.securevault.main.exception.InvalidCredentialsException;
import com.securevault.main.exception.InvalidTokenException;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.exception.TokenReuseException;
import com.securevault.main.exception.UnauthorizedException;
import com.securevault.main.exception.UnverifiedEmailException;
import com.securevault.main.security.JwtTokenProvider;
import com.securevault.main.service.AuthService;
import com.securevault.main.service.EmailVerificationService;
import com.securevault.main.service.MessageSourceService;
import com.securevault.main.service.UserService;
import com.securevault.main.util.ApiEndpoints;

import jakarta.servlet.http.HttpServletRequest;
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
	private final HttpServletRequest httpServletRequest;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping(ApiEndpoints.AUTH_SEND_EMAIL_VERIFICATION_URL)
	public ResponseEntity<SuccessResponse> sendEmailVerification(
			@RequestBody @Valid SendEmailVerificationRequest request) {
		try {
			emailVerificationService.sendEmailVerification(request);
			return ResponseEntity.ok(SuccessResponse.of(messageSourceService.get("email_verification_sent"), null));
		} catch (BadRequestException e) {
			log.error("Bad request during email verification send: {}", e.getMessage());

			String exceptionMessage = e.getMessage() != null ? e.getMessage()
					: messageSourceService.get("email_verification_failed");

			if (e.getNextAction() != null) {
				throw new BadRequestException(exceptionMessage).withNextAction(e.getNextAction());
			}

			throw new BadRequestException(exceptionMessage);
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

	@GetMapping(ApiEndpoints.AUTH_VERIFY_EMAIL_URL)
	public ResponseEntity<SuccessResponse> verifyEmail(
			@RequestParam String token,
			@RequestParam @Valid String email) {
		try {
			emailVerificationService.verifyEmail(token, email);
			return ResponseEntity.ok(SuccessResponse.of(messageSourceService.get("email_verified"), null));
		} catch (BadRequestException e) {
			log.error("Bad request during email verification: {}", e.getMessage());

			String exceptionMessage = e.getMessage() != null ? e.getMessage()
					: messageSourceService.get("invalid_verification");

			throw new BadRequestException(exceptionMessage);
		} catch (NotFoundException e) {
			log.error("Token not found during email verification: {}", e.getMessage());
			throw new NotFoundException(messageSourceService.get("verification_link_invalid"));
		} catch (Exception e) {
			log.error("Unexpected error during email verification: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("service_unavailable"));
		}
	}

	@PostMapping(ApiEndpoints.AUTH_FINISH_REGISTRATION_URL)
	public ResponseEntity<SuccessResponse> finishRegistration(@RequestBody @Valid FinishRegistrationRequest request)
			throws BindException {
		try {
			userService.finishRegistration(request);
			return ResponseEntity.ok(SuccessResponse.of(messageSourceService.get("registration_completed"), null));
		} catch (BadRequestException e) {
			log.error("Bad request during registration: {}", e.getMessage());
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during registration: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("service_unavailable"));
		}
	}

	@PostMapping(ApiEndpoints.AUTH_LOGIN_URL)
	public ResponseEntity<SuccessResponse> login(@RequestBody @Validated final LoginRequest request) {
		try {
			TokenResponse loginTokenResponse = authService.login(request.getEmail(), request.getMasterPasswordHash(),
					request.getRememberMe());

			return ResponseEntity.ok(SuccessResponse.of(messageSourceService.get("login_success"), loginTokenResponse));
		} catch (BadRequestException e) {
			log.error("Bad request during login: {}", e.getMessage());
			throw new BadRequestException(e.getMessage());
		} catch (InvalidCredentialsException e) {
			log.error("Invalid credentials for user: {}", request.getEmail());
			throw new BadRequestException(messageSourceService.get("invalid_credentials"));
		} catch (AccountLockedException e) {
			log.error("Account locked for user: {}", request.getEmail());
			throw new BadRequestException(messageSourceService.get("account_locked"));
		} catch (UnverifiedEmailException e) {
			log.error("Unverified email for user: {}", request.getEmail());
			throw new BadRequestException(messageSourceService.get("email_not_verified"));
		} catch (NotFoundException e) {
			log.error("User not found: {}", request.getEmail());
			throw new NotFoundException(messageSourceService.get("user_not_found"));
		} catch (Exception e) {
			log.error("Unexpected error during login: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("service_unavailable"));
		}
	}

	@GetMapping(ApiEndpoints.AUTH_REFRESH_TOKEN_URL)
	public ResponseEntity<SuccessResponse> refresh(@CookieValue("sv.rftkn") @Validated final String refreshToken) {
		try {
			TokenResponse refreshTokenResponse = authService.refreshFromCookie(refreshToken);

			return ResponseEntity.ok(SuccessResponse.of(messageSourceService.get("token_refreshed"), refreshTokenResponse));
		} catch (InvalidTokenException e) {
			log.error("Invalid refresh token");
			throw new InvalidTokenException(messageSourceService.get("invalid_refresh_token"));
		} catch (TokenReuseException e) {
			log.error("Refresh token reuse attempt");
			throw new TokenReuseException(messageSourceService.get("token_reuse_detected"));
		} catch (NotFoundException e) {
			log.error("User not found for refresh token");
			throw new UnauthorizedException(messageSourceService.get("unauthorized"));
		} catch (Exception e) {
			log.error("Unexpected error during token refresh: {}", e.getMessage());
			throw new UnauthorizedException(messageSourceService.get("unauthorized"));
		}
	}

	@GetMapping(ApiEndpoints.AUTH_LOGOUT_URL)
	public ResponseEntity<SuccessResponse> logout() {
		String token = jwtTokenProvider.extractJwtFromRequest(httpServletRequest);

		authService.logout(token);

		return ResponseEntity.ok(SuccessResponse.of(messageSourceService.get("logout_successfully"), null));
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
