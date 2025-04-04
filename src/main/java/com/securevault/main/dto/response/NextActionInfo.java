package com.securevault.main.dto.response;

import com.securevault.main.enums.NextAction;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NextActionInfo {
	private final String type;
	private final String redirectUrl;

	public static NextActionInfo of(NextAction action, String redirectUrl) {
		return NextActionInfo.builder()
				.type(action.getAction())
				.redirectUrl(redirectUrl)
				.build();
	}
}