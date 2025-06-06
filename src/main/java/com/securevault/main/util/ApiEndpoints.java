package com.securevault.main.util;

public final class ApiEndpoints {
	public static final String AUTH_BASE_URL = "/auth";
	public static final String AUTH_FINISH_REGISTRATION_URL = "/finish-registration";
	public static final String AUTH_LOGIN_URL = "/login";
	public static final String AUTH_LOGOUT_URL = "/logout";
	public static final String AUTH_REFRESH_TOKEN_URL = "/refresh-token";
	public static final String AUTH_SEND_EMAIL_VERIFICATION_URL = "/send-email-verification";
	public static final String AUTH_VERIFY_EMAIL_URL = "/verify-email";

	public static final String USER_BASE_URL = "/user";
	public static final String USER_GET_AUTHENTICATED_USER_URL = "/me";

	public static final String HEALTH_BASE_URL = "/health";
	public static final String HEALTH_CHECK_URL = "/check";
	public static final String HEALTH_LIVENESS_URL = "/live";
	public static final String HEALTH_READINESS_URL = "/ready";
}
