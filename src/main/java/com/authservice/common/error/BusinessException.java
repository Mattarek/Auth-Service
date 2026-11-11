package com.authservice.common.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final ErrorCode errorCode;

	public BusinessException(final ErrorCode errorCode) {
		super(errorCode.getTitle());
		this.errorCode = errorCode;
	}

	public BusinessException(final ErrorCode errorCode, final String detail) {
		super(detail);
		this.errorCode = errorCode;
	}
}
