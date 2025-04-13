package com.securevault.main.dto.response;

import com.securevault.main.enums.ResponseStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SuccessResponse extends AbstractBaseResponse<SuccessResponse> {
	private String message;
	private Object data;

	public static <T> SuccessResponse of(String message, T data) {
		return SuccessResponse.builder()
				.status(ResponseStatus.SUCCESS.getValue())
				.message(message)
				.data(data)
				.build();
	}
}
