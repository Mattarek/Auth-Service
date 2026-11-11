package com.authservice.common.error;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	private static final String INTERNAL_ERROR_DETAIL = "An unexpected error occurred";
	private static final Map<Integer, ErrorCode> ERROR_CODES_BY_STATUS = Map.of(
			HttpStatus.NOT_FOUND.value(), ErrorCode.RESOURCE_NOT_FOUND,
			HttpStatus.METHOD_NOT_ALLOWED.value(), ErrorCode.METHOD_NOT_ALLOWED,
			HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), ErrorCode.UNSUPPORTED_MEDIA_TYPE
	);

	private static boolean hasErrorCode(final ProblemDetail problem) {
		return problem.getProperties() != null && problem.getProperties().containsKey("code");
	}

	private static ErrorCode defaultErrorCodeFor(final HttpStatusCode statusCode) {
		final ErrorCode errorCode = ERROR_CODES_BY_STATUS.get(statusCode.value());
		if (errorCode != null) {
			return errorCode;
		}
		return statusCode.is5xxServerError() ? ErrorCode.INTERNAL_ERROR : ErrorCode.MALFORMED_REQUEST;
	}

	@ExceptionHandler(BusinessException.class)
	ProblemDetail handleBusinessException(final BusinessException ex) {
		log.debug("Business error: {} - {}", ex.getErrorCode(), ex.getMessage());
		return toProblemDetail(ex.getErrorCode(), ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpectedException(final Exception ex) {
		log.error("Unexpected error", ex);
		return toProblemDetail(ErrorCode.INTERNAL_ERROR, INTERNAL_ERROR_DETAIL);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
			final @NonNull Exception ex,
			final Object body,
			final @NonNull HttpHeaders headers,
			final @NonNull HttpStatusCode statusCode,
			final @NonNull WebRequest request
	) {
		final ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
		if (response != null && response.getBody() instanceof final ProblemDetail problem && !hasErrorCode(problem)) {
			final ErrorCode errorCode = defaultErrorCodeFor(statusCode);
			problem.setTitle(errorCode.getTitle());
			problem.setProperty("code", errorCode.name());
		}
		return response;
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			final MethodArgumentNotValidException methodArgumentNotValidException,
			final @NonNull HttpHeaders headers,
			final @NonNull HttpStatusCode httpStatusCode,
			final @NonNull WebRequest webRequest
	) {
		final List<FieldViolation> violations =
				methodArgumentNotValidException.getBindingResult()
						.getFieldErrors()
						.stream()
						.map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
						.toList();

		final ProblemDetail problem = toProblemDetail(
				ErrorCode.VALIDATION_FAILED,
				"Request contains invalid fields"
		);

		problem.setProperty("errors", violations);

		return handleExceptionInternal(
				methodArgumentNotValidException,
				problem,
				headers,
				httpStatusCode,
				webRequest
		);
	}

	private ProblemDetail toProblemDetail(final ErrorCode errorCode, final String detail) {
		final ProblemDetail problem = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);
		problem.setTitle(errorCode.getTitle());
		problem.setProperty("code", errorCode.name());
		return problem;
	}
}
