package com.securevault.main.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseStatus {
	SUCCESS("success"),
	ERROR("error");

	private final String value;

	public static ResponseStatus fromValue(String value) {
		for (ResponseStatus status : ResponseStatus.values()) {
			if (status.getValue().equalsIgnoreCase(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unexpected value: " + value);
	}
}
