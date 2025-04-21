package com.securevault.main.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.securevault.main.enums.NextAction;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@Getter
@Setter
public abstract class AbstractBaseResponse<T extends AbstractBaseResponse<T>> {
	protected String status;
	protected NextActionInfo nextAction;

	protected AbstractBaseResponse() {
	}

	@SuppressWarnings("unchecked")
	public T withNextAction(NextAction nextAction) {
		this.nextAction = NextActionInfo.of(nextAction);
		return (T) this;
	}
}
