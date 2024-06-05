package com.securevault.main.service;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSourceService {
	private final MessageSource messageSource;

	/**
	 * Get message from message source by key
	 * 
	 * @param code
	 * @param params
	 * @param locale
	 * @return message String
	 */
	public String get(final String code, final Object[] params, final Locale locale) {
		try {
			return messageSource.getMessage(code, params, locale);
		} catch (Exception e) {
			log.warn("Translation message not found ({}: {})", locale, code);
			return code;
		}
	}

	/**
	 * Get message from message source by key
	 * 
	 * @param code
	 * @param params
	 * @return message String
	 */
	public String get(final String code, final Object[] params) {
		return get(code, params, LocaleContextHolder.getLocale());
	}

	/**
	 * Get message from message source byb key
	 * 
	 * @param code
	 * @param locale
	 * @return message String
	 */
	public String get(final String code, final Locale locale) {
		return get(code, new Object[0], locale);
	}

	/**
	 * Get message from message source by key.
	 * 
	 * @param code
	 * @return message String
	 */
	public String get(final String code) {
		return get(code, new Object[0], LocaleContextHolder.getLocale());
	}

}
