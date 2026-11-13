package com.authservice.common.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void shouldMapBusinessExceptionToProblemDetail() throws Exception {
		// when
		final ResultActions result = mockMvc.perform(get("/test/business"));

		// then
		result.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
				.andExpect(jsonPath("$.title").value("Resource not found"))
				.andExpect(jsonPath("$.detail").value("Custom detail"))
				.andExpect(jsonPath("$.instance").value("/test/business"));
	}

	@Test
	void shouldHideDetails_whenUnexpectedExceptionOccurs() throws Exception {
		// when
		final ResultActions result = mockMvc.perform(get("/test/unexpected"));

		// then
		result.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
				.andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
				.andExpect(content().string(not(containsString("secret"))));
	}

	@Test
	void shouldReturnFieldErrors_whenRequestIsInvalid() throws Exception {
		// given
		final String invalidBody = "{\"name\": \"\"}";

		// when
		final ResultActions result = mockMvc.perform(post("/test/validated")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidBody));

		// then
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.errors[0].field").value("name"))
				.andExpect(jsonPath("$.errors[0].message").value("must not be blank"));
	}

	@Test
	void shouldReturnNotFound_whenEndpointDoesNotExist() throws Exception {
		// when
		final ResultActions result = mockMvc.perform(get("/test/unknown"));

		// then
		result.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void shouldReturnMethodNotAllowed_whenHttpMethodIsNotSupported() throws Exception {
		// when
		final ResultActions result = mockMvc.perform(get("/test/body"));

		// then
		result.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
	}

	@Test
	void shouldReturnMalformedRequest_whenJsonIsInvalid() throws Exception {
		// given
		final String malformedJson = "{ not valid json";

		// when
		final ResultActions result = mockMvc.perform(post("/test/body")
				.contentType(MediaType.APPLICATION_JSON)
				.content(malformedJson));

		// then
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
	}

	@Test
	void shouldReturnUnsupportedMediaType_whenContentTypeIsNotJson() throws Exception {
		// when
		final ResultActions result = mockMvc.perform(post("/test/body")
				.contentType(MediaType.TEXT_PLAIN)
				.content("plain text"));

		// then
		result.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
	}

	@RestController
	static class TestController {
		@GetMapping("/test/business")
		void throwBusinessException() {
			throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Custom detail");
		}

		@GetMapping("/test/unexpected")
		void throwUnexpectedException() {
			throw new IllegalStateException("Connection failed, secret password: admin123");
		}

		@PostMapping("/test/body")
		void acceptBody(@RequestBody final TestRequest request) {
		}

		@PostMapping("/test/validated")
		void acceptValidBody(@Valid @RequestBody final TestRequest request) {
		}
	}

	record TestRequest(@NotBlank String name) {
	}
}