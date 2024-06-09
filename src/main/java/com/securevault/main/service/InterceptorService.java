package com.securevault.main.service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterceptorService implements HandlerInterceptor {

	public boolean preHandle(@NonNull final HttpServletRequest request,
			@NonNull final HttpServletResponse response,
			@NonNull final Object handler) {
		HandlerMethod handlerMethod;

		try {
			handlerMethod = (HandlerMethod) handler;
		} catch (ClassCastException e) {
			return true;
		}

		validateQueryParams(request, handlerMethod);

		return true;
	}

	/**
	 * Validation of the request params or model attribute parameters to check
	 * unhandled ones.
	 * 
	 * @param request - Request information for HTTP servlets
	 * @param handler - Encapsulates information about a handler method consisting
	 *                of a method
	 */
	private void validateQueryParams(final HttpServletRequest request, final HandlerMethod handler) {
		List<String> queryParams = Collections.list(request.getParameterNames());
		MethodParameter[] methodParameters = handler.getMethodParameters();
		List<String> expectedParams = new ArrayList<>(methodParameters.length);

		for (MethodParameter methodParameter : methodParameters) {
			RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
			if (requestParam != null) {
				if (StringUtils.hasText(requestParam.name())) {
					expectedParams.add(requestParam.name());
				} else {
					methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
					expectedParams.add(methodParameter.getParameterName());
				}
			}

			ModelAttribute modelAttribute = methodParameter.getParameterAnnotation(ModelAttribute.class);
			if (modelAttribute != null) {
				methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
				String modelAttributeName = methodParameter.getParameterName();
				expectedParams.add(modelAttributeName);
			}
		}

		queryParams.removeAll(expectedParams);
		if (!queryParams.isEmpty()) {
			log.error("Unexpected parameters: {}", queryParams);
			throw new InvalidParameterException("unexpected parameter: " + queryParams);
		}
	}

}
