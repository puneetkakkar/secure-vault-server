package com.securevault.main.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;

import com.securevault.main.dto.request.auth.FinishRegistrationRequest;
import com.securevault.main.entity.User;
import com.securevault.main.exception.BadRequestException;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.repository.UserRepository;
import com.securevault.main.security.JwtUserDetails;
import com.securevault.main.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements UserDetailsService {
	private static final int DEFAULT_KDF_ITERATIONS = 600000;
	private final UserRepository userRepository;
	private final RoleService roleService;
	private final MessageSourceService messageSourceService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(final String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
						new String[] { messageSourceService.get("user") })));

		return JwtUserDetails.create(user);
	}

	public UserDetails loadUserById(final String id) {
		User user = userRepository.findById(UUID.fromString(id))
				.orElseThrow(() -> new NotFoundException(
						messageSourceService.get("not_found_with_param", new String[] { messageSourceService.get("user") })));

		return JwtUserDetails.create(user);
	}

	public Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public User getUser() {
		Authentication authentication = getAuthentication();
		if (authentication.isAuthenticated()) {
			try {
				return findById(getPrincipal(authentication).getId());
			} catch (ClassCastException | NotFoundException e) {
				log.warn("[JWT] User details not found!");
				throw new BadCredentialsException(messageSourceService.get("bad_credentials"));
			}
		} else {
			log.warn("[JWT] User not authenticated!");
			throw new BadCredentialsException(messageSourceService.get("bad_credentials"));
		}
	}

	public User findById(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
						new String[] { messageSourceService.get("user") })));
	}

	public User findById(String id) {
		return findById(UUID.fromString(id));
	}

	public User findByEmail(final String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(
				messageSourceService.get("not_found_with_param", new String[] { messageSourceService.get("user") })));
	}

	public JwtUserDetails getPrincipal(final Authentication authentication) {
		return (JwtUserDetails) authentication.getPrincipal();
	}

	public User getOrCreateUnverifiedUser(String email, String name) {
		try {
			User existingUser = findByEmail(email);

			return existingUser;
		} catch (NotFoundException e) {
			// Create new unverified user
			User newUser = User.builder()
					.email(email)
					.name(name)
					.build();
			return userRepository.save(newUser);
		}
	}

	public void markEmailAsVerified(User user) {
		user.setEmailVerifiedAt(LocalDateTime.now());
		userRepository.save(user);
	}

	public void finishRegistration(final FinishRegistrationRequest request) throws BindException {
		log.info("Processing registration request for user with email: {}", request.getEmail());

		// Find existing user
		User user = findByEmail(request.getEmail());

		// Verify email is verified
		if (user.getEmailVerifiedAt() == null) {
			throw new BadRequestException(messageSourceService.get("email_not_verified"));
		}

		// Check if user is already registered
		if (user.getMasterPasswordHash() != null) {
			throw new BadRequestException(messageSourceService.get("user_already_registered"));
		}

		// Update user with registration data
		user.setMasterPasswordHash(passwordEncoder.encode(request.getMasterPasswordHash()));
		user.setMasterPasswordHint(request.getMasterPasswordHint());
		user.setUserKey(request.getUserKey());
		user.setKdfIterations(request.getKdfIterations() != null ? request.getKdfIterations() : DEFAULT_KDF_ITERATIONS);
		user.setRoles(List.of(roleService.findByName(Constants.RoleEnum.USER)));

		// Save updated user
		userRepository.save(user);

		log.info("Registration completed for user: {}", user.getEmail());
	}

	public void incrementFailedLoginAttempts(String email) {
		User user = findByEmail(email);
		user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
		
		if (user.getFailedLoginAttempts() >= 5) {
			user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
		}
		
		userRepository.save(user);
	}

	public void resetFailedLoginAttempts(String email) {
		User user = findByEmail(email);
		user.setFailedLoginAttempts(0);
		user.setLockedUntil(null);
		userRepository.save(user);
	}

}
