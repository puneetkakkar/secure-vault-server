package com.securevault.main.configuration.api;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VersionedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	private final ApiVersionProperties apiVersionProperties;

	private final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+){0,2}$");

	@Override
	protected RequestMappingInfo getMappingForMethod(@NonNull Method method, @NonNull Class<?> handlerType) {
		RequestMappingInfo info = this.createRequestMappingInfo(method);

		if (info != null) {
			RequestMappingInfo typeInfo = this.createRequestMappingInfo(handlerType);
			if (typeInfo != null) {
				info = typeInfo.combine(info);
			}

			ApiVersion apiVersion = AnnotationUtils.getAnnotation(method, ApiVersion.class);
			if (apiVersion == null) {
				apiVersion = AnnotationUtils.getAnnotation(handlerType, ApiVersion.class);
			}

			if (apiVersion != null) {
				String version = apiVersion.value().trim();
				this.checkVersionNumber(version, method);

				String prefix = "/v" + version;

				if (StringUtils.hasText(apiVersionProperties.getUriPrefix())) {
					prefix = apiVersionProperties.getUriPrefix().trim() + prefix;
				}

				info = RequestMappingInfo.paths(prefix).options(getBuilderConfiguration()).build().combine(info);
			}
		}

		return info;
	}

	private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
		RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
		RequestCondition<?> condition = (element instanceof Class ? getCustomTypeCondition((Class<?>) element)
				: getCustomMethodCondition((Method) element));

		return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
	}

	private void checkVersionNumber(String version, Object targetMethodOrType) {
		if (!matchVersionNumber(version)) {
			throw new IllegalArgumentException(
					String.format("Invalid version number: @ApiVersion(\"%s\") at %s", version, targetMethodOrType));
		}
	}

	private boolean matchVersionNumber(String version) {
		return version.length() != 0 && VERSION_NUMBER_PATTERN.matcher(version).find();
	}

}
