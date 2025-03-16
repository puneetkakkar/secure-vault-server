package com.securevault.main.security;

import java.util.Date;

import javax.crypto.SecretKey;

import com.securevault.main.service.MessageSourceService;
import com.securevault.main.util.ApiEndpoints;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.securevault.main.entity.JwtToken;
import com.securevault.main.entity.User;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.service.JwtTokenService;
import com.securevault.main.service.UserService;
import com.securevault.main.util.Constants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {
    private final UserService userService;
    private final String appSecret;

    @Getter
    private final Long tokenExpiresIn;
    private final MessageSourceService messageSourceService;

    @Getter
    private Long refreshTokenExpiresIn;

    private final Long rememberMeTokenExpiresIn;

    private final JwtTokenService jwtTokenService;
    private final HttpServletRequest httpServletRequest;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") final String appSecret,
            @Value("${app.jwt.token.expires-in}") final Long tokenExpiresIn,
            @Value("${app.jwt.refresh-token.expires-in}") final Long refreshTokenExpiresIn,
            @Value("${app.jwt.remember-me.expires-in}") final Long rememberMeTokenExpiresIn,
            final UserService userService,
            final JwtTokenService jwtTokenService,
            final HttpServletRequest httpServletRequest, MessageSourceService messageSourceService) {
        this.userService = userService;
        this.appSecret = appSecret;
        this.tokenExpiresIn = tokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.rememberMeTokenExpiresIn = rememberMeTokenExpiresIn;
        this.jwtTokenService = jwtTokenService;
        this.httpServletRequest = httpServletRequest;
        this.messageSourceService = messageSourceService;
    }

    public String generateTokenByUserId(final String id, final Long expires) {
        String token = Jwts.builder().subject(id).issuedAt(new Date()).expiration(getExpireDate(expires))
                .signWith(getSigningKey(), Jwts.SIG.HS256).compact();

        log.trace("Token is added to the local cache for userID: {}, ttl: {}", id, expires);

        return token;
    }

    public JwtUserDetails getPrincipal(final Authentication authentication) {
        return userService.getPrincipal(authentication);
    }

    public String getUserIdFromToken(final String token) {
        Claims claims = parseToken(token).getPayload();

        return claims.getSubject();
    }

    public User getUserFromToken(final String token) {
        try {
            return userService.findById(getUserIdFromToken(token));
        } catch (Exception e) {
            return null;
        }
    }

    public JwtToken getTokenOrRefreshToken(final String token) {
        try {
            return jwtTokenService.findByTokenOrRefreshToken(token);
        } catch (NotFoundException e) {
            log.error("[JWT] Token could not be found in Redis");
            return null;
        }
    }

    public String generateJwt(final String id) {
        return generateTokenByUserId(id, tokenExpiresIn);
    }

    public String generateRefresh(final String id) {
        return generateTokenByUserId(id, refreshTokenExpiresIn);
    }

    public boolean validateToken(final String token) {
        return validateToken(token, true);
    }

    public boolean validateToken(final String token, final boolean isHttp) {
        parseToken(token);

        JwtToken jwtToken = getTokenOrRefreshToken(token);

        if (jwtToken == null) {
            return false;
        }

        if (isHttp && !httpServletRequest.getHeader("User-agent").equals(jwtToken.getUserAgent())) {
            log.error("[JWT] User-agent is not matched");
            return false;
        }

        return !isTokenExpired(token);
    }

    public boolean validateToken(final String token, final HttpServletRequest httpServletRequest) {
        final String accessDeniedMessage = messageSourceService.get("access_denied");
        final String requestURI = httpServletRequest.getRequestURI();

        try {
            boolean isTokenValid = validateToken(token);
            if (!isTokenValid) {
                logErrorAndSetAttribute("Token could not be found in local cache", httpServletRequest, accessDeniedMessage);
            }
            return true;
        } catch (UnsupportedJwtException e) {
            logAndThrowAccessDenied("Unsupported JWT token!", httpServletRequest, accessDeniedMessage, e);
        } catch (SignatureException | MalformedJwtException e) {
            logAndThrowAccessDenied("Invalid JWT token!", httpServletRequest, accessDeniedMessage, e);
        } catch (ExpiredJwtException e) {
            handleExpiredToken(e, httpServletRequest, accessDeniedMessage, requestURI);
        } catch (IllegalArgumentException e) {
            logAndThrowAccessDenied("JWT claims string is empty", httpServletRequest, accessDeniedMessage, e);
        }

        return false;
    }

    public void setRememberMe() {
        this.refreshTokenExpiresIn = rememberMeTokenExpiresIn;
    }

    public String extractJwtFromBearerString(final String bearer) {
        if (StringUtils.hasText(bearer) && bearer.startsWith(String.format("%s ", Constants.TOKEN_TYPE))) {
            return bearer.substring(Constants.TOKEN_TYPE.length() + 1);
        }

        return null;
    }

    public String extractJwtFromRequest(final HttpServletRequest request) {
        return extractJwtFromBearerString(request.getHeader(Constants.TOKEN_HEADER));
    }

    /**
     * Parsing token
     *
     * @param token String jwt token to parse
     * @return Jws claims object
     */
    private Jws<Claims> parseToken(final String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
    }

    private boolean isTokenExpired(final String token) {
        return parseToken(token).getPayload().getExpiration().before(new Date());
    }

    private Date getExpireDate(final Long expires) {
        return new Date(new Date().getTime() + expires);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(appSecret.getBytes());
    }

    private void handleExpiredToken(ExpiredJwtException e, HttpServletRequest httpServletRequest, String accessDeniedMessage, String requestURI) {
        if (requestURI.contains(ApiEndpoints.AUTH_REFRESH_TOKEN_URL)) {
            log.info("[JWT Refresh] Expired JWT token detected. Token will be refreshed.");
        } else {
            logErrorAndSetAttribute("Expired JWT token!", httpServletRequest, accessDeniedMessage);
            throw new AccessDeniedException(e.getMessage());
        }
    }

    private void logAndThrowAccessDenied(String message, HttpServletRequest httpServletRequest, String accessDeniedMessage) {
        logErrorAndSetAttribute(message, httpServletRequest, accessDeniedMessage);
        throw new AccessDeniedException(accessDeniedMessage);
    }

    private void logAndThrowAccessDenied(String message, HttpServletRequest httpServletRequest, String accessDeniedMessage, Exception e) {
        logErrorAndSetAttribute(message, httpServletRequest, accessDeniedMessage);
        throw new AccessDeniedException(e.getMessage());
    }

    private void logErrorAndSetAttribute(String logMessage, HttpServletRequest request, String message) {
        log.error("[JWT] {}", logMessage);
        request.setAttribute("access_denied", message);
    }
}
