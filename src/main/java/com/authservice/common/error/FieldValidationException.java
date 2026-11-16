package com.authservice.common.error;

import lombok.Getter;

import java.util.List;

@Getter
public class FieldValidationException extends BusinessException {
	private final List<FieldViolation> violations;

	public FieldValidationException(final List<FieldViolation> violations) {
		super(ErrorCode.VALIDATION_FAILED);
		this.violations = List.copyOf(violations);
	}
}
