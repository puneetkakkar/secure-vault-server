package com.securevault.main.dto.response;

import com.securevault.main.enums.ErrorCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
public class ErrorResponse extends AbstractBaseResponse {
    private ErrorCode code;
    private String message;
    private Instant timestamp;
}
