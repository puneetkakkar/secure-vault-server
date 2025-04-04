package com.securevault.main.dto.response;

import java.time.Instant;

import com.securevault.main.enums.ErrorCode;
import com.securevault.main.enums.ResponseStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ErrorResponse extends AbstractBaseResponse<ErrorResponse> {
	private ErrorCode code;
	private String message;
	private Instant timestamp;

	public static ErrorResponse of(ErrorCode code, String message) {
		return ErrorResponse.builder()
				.status(ResponseStatus.ERROR.getValue())
				.code(code)
				.message(message)
				.timestamp(Instant.now())
				.build();
	}
}
