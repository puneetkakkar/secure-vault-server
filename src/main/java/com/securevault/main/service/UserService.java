package com.securevault.main.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.securevault.main.dto.request.auth.RegisterRequest;
import com.securevault.main.dto.request.user.AbstractBaseCreateUserRequest;
import com.securevault.main.entity.User;
import com.securevault.main.event.UserEmailVerificationSendEvent;
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
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final MessageSourceService messageSourceService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailVerficationTokenService emailVerficationTokenService;

    @Override
    public UserDetails loadUserByUsername(final String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));

        return JwtUserDetails.create(user);
    }

    public UserDetails loadUserById(final String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException(
                        messageSourceService.get("not_found_with_param", new String[]{messageSourceService.get("user")})));

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
                        new String[]{messageSourceService.get("user")})));
    }

    public User findById(String id) {
        return findById(UUID.fromString(id));
    }

    public User findByEmail(final String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException(
                messageSourceService.get("not_found_with_param", new String[]{messageSourceService.get("user")})));
    }

    public JwtUserDetails getPrincipal(final Authentication authentication) {
        return (JwtUserDetails) authentication.getPrincipal();
    }

    public void register(final RegisterRequest request) throws BindException {
        log.info("Registering user with email: {}", request.getEmail());

        User user = createUser(request);
        user.setRoles(List.of(roleService.findByName(Constants.RoleEnum.USER)));
        userRepository.save(user);

        emailVerificationEventPublisher(user);

        log.info("User registered with email: {}, {}", user.getEmail(), user.getId());

    }

    public void verifyEmail(String token) {
        log.info("Verifying e-mail with token: {}", token);
        User user = emailVerficationTokenService.getUserByToken(token);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        emailVerficationTokenService.deleteByUserId(user.getId());
        log.info("E-mail verified with token: {}", token);
    }

    protected void emailVerificationEventPublisher(User user) {
        user.setEmailVerificationToken(emailVerficationTokenService.create(user));
        userRepository.save(user);
        eventPublisher.publishEvent(new UserEmailVerificationSendEvent(this, user));
    }

    private User createUser(AbstractBaseCreateUserRequest request) throws BindException {
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            log.error("User with email: {} already exists", request.getEmail());
            bindingResult
                    .addError(new FieldError(bindingResult.getObjectName(),
                            "email",
                            bindingResult.getFieldValue("email"),
                            false,
                            null,
                            null,
                            messageSourceService.get("unique_email")));
        });

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        return User.builder().email(request.getEmail())
                .masterPasswordHash(passwordEncoder.encode(request.getMasterPasswordHash()))
                .masterPasswordHint(request.getMasterPasswordHint()).name(request.getName())
                .kdfIterations(request.getKdfIterations()).build();

    }

}
