package com.securevault.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.main.configuration.api.ApiVersion;
import com.securevault.main.dto.response.SuccessResponse;
import com.securevault.main.exception.BadRequestException;
import com.securevault.main.exception.InvalidTokenException;
import com.securevault.main.service.MessageSourceService;
import com.securevault.main.util.ApiEndpoints;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(ApiEndpoints.USER_BASE_URL)
@ApiVersion("1")
@RequiredArgsConstructor
public class UserController {
	private final MessageSourceService messageSourceService;

	@GetMapping(ApiEndpoints.USER_GET_AUTHENTICATED_USER_URL)
	public ResponseEntity<SuccessResponse> getAuthenticatedUser() {
		try {
			return ResponseEntity.ok(SuccessResponse.of(messageSourceService.get("user_retrieved"), null));
		} catch (Exception e) {
			log.error("Error getting authenticated user: {}", e.getMessage());
			throw new BadRequestException(messageSourceService.get("service_unavailable"));
		}
	}
}
