package com.securevault.main.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.securevault.main.dto.request.auth.RegisterRequest;
import com.securevault.main.dto.request.user.AbstractBaseCreateUserRequest;
import com.securevault.main.entity.User;
import com.securevault.main.repository.UserRepository;
import com.securevault.main.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
	private final UserRepository userRepository;
	private final RoleService roleService;
	private final MessageSourceService messageSourceService;

	public User register(final RegisterRequest request) throws BindException {
		log.info("Registering user with email: {}", request.getEmail());

		User user = createUser(request);
		user.setRoles(List.of(roleService.findByName(Constants.RoleEnum.USER)));
		userRepository.save(user);

		log.info("User registered with email: {}, {}", user.getEmail(), user.getId());

		return user;
	}

	private User createUser(AbstractBaseCreateUserRequest request) throws BindException {
		BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
		userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
			log.error("User with email: {} already exists", request.getEmail());
			bindingResult
					.addError(new FieldError(bindingResult.getObjectName(), "email", messageSourceService.get("unique_email")));
		});

		if (bindingResult.hasErrors()) {
			throw new BindException(bindingResult);
		}

		return User.builder().email(request.getEmail()).masterPasswordHash(request.getMasterPasswordHash())
				.masterPasswordHint(request.getMasterPasswordHint()).name(request.getName())
				.kdfIterations(request.getKdfIterations()).build();

	}

}
