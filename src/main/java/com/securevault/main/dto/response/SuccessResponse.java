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

	public static SuccessResponse of(String message) {
		return SuccessResponse.builder()
				.status(ResponseStatus.SUCCESS.getValue())
				.message(message)
				.build();
	}
}
