package com.securevault.main.dto.response;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class DetailedErrorResponse extends ErrorResponse {
    private Map<String, List<Map<String, String>>> errors;
}
