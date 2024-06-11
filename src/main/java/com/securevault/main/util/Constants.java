package com.securevault.main.util;

import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;;

public final class Constants {
	public static final String TOKEN_HEADER = "Authorization";
	public static final String TOKEN_TYPE = "Bearer";
	public static final int EMAIL_VERIFICATION_TOKEN_LENGTH = 64;

	@Getter
	@AllArgsConstructor
	public enum RoleEnum {
		ADMIN("ADMIN"), USER("USER");

		private final String value;

		public static RoleEnum get(final String name) {
			return Stream.of(RoleEnum.values())
					.filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase())).findFirst()
					.orElseThrow(() -> new IllegalArgumentException(String.format("Invalid role name: %s", name)));
		}

	}

}
