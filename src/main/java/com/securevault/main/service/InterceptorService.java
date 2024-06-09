// package com.securevault.main.service;

// import java.io.IOException;

// import org.springframework.lang.NonNull;
// import org.springframework.stereotype.Component;
// import org.springframework.web.servlet.HandlerInterceptor;

// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class InterceptorService implements HandlerInterceptor {

// 	@Override
// 	public boolean preHandle(@NonNull final HttpServletRequest request, @NonNull final HttpServletResponse response,
// 			@NonNull final Object handler) throws ServletException, IOException {

// 		// // log.info("REQUESTED_VERSION: {}", requestedVersion);

// 		// String annotatedVersion = getAnnotatedVersion(handler);

// 		// // log.info("ANNOTATED_VERSION: {}", annotatedVersion);

// 		// if (annotatedVersion == null || annotatedVersion.isEmpty()) {
// 		// return true;
// 		// }

// 		// String requestURI = request.getRequestURI();
// 		// if (annotatedVersion != null && !requestURI.contains("/" + annotatedVersion +
// 		// "/")) {
// 		// requestURI = requestURI.replaceFirst("/api/", "/api/" + annotatedVersion +
// 		// "/");
// 		// request.getRequestDispatcher(requestURI).forward(request, response);
// 		// }

// 		return true;

// 	}

// 	// private String extractVersionFromRequest(HttpServletRequest request) {
// 	// String uri = request.getRequestURI();
// 	// Matcher matcher = VERSION_PATTERN.matcher(uri);
// 	// if (matcher.find()) {
// 	// return matcher.group(1);
// 	// }

// 	// return null;
// 	// }

// }
