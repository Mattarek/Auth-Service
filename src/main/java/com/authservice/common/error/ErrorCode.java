package com.authservice.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
	MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Malformed request"),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type");

	private final HttpStatus status;
	private final String title;
}
