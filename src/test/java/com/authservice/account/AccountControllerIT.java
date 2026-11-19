package com.authservice.account;

import com.authservice.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AccountControllerIT {
	private static final String ACCOUNTS_URL = "/api/v1/accounts";
	private static final String VALID_PASSWORD = "correcthorse1";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldCreatePendingAccountWhenRequestIsValid() throws Exception {
		// WHEN
		final ResultActions resultActions = register("New.User@Example.com", VALID_PASSWORD);

		// THEN
		resultActions.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.email").value("new.user@example.com"))
				.andExpect(jsonPath("$.status").value("PENDING_ACTIVATION"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void shouldReturnConflictWhenEmailIsTakenIgnoringCase() throws Exception {
		// GIVEN
		register("taken@example.com", VALID_PASSWORD).andExpect(status().isCreated());

		// WHEN
		final ResultActions result = register("TAKEN@example.com", VALID_PASSWORD);

		// then
		result.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_TAKEN"));
	}

	@Test
	void shouldReturnFieldErrorWhenEmailIsInvalid() throws Exception {
		// WHEN
		final ResultActions resultActions = register("not-an-email", VALID_PASSWORD);

		// THEN
		resultActions.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.errors[0].field").value("email"))
				.andExpect(jsonPath("$.errors[0].message").value("must be a well-formed email address")
				);
	}

	@Test
	void shouldReturnPasswordFieldErrors_whenPasswordViolatesPolicy() throws Exception {
		// when
		final ResultActions result = register("weak-password@example.com", "short");
		// then
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.errors[0].field").value("password"));
	}

	@Test
	void shouldDenyAccessWhenEndpointIsNotExplicitlyPermitted() throws Exception {
		// WHEN
		final ResultActions resultActions = mockMvc.perform(get(ACCOUNTS_URL));

		// THEN
		resultActions.andExpect(status().isForbidden());
	}

	private ResultActions register(final String email, final String password) throws Exception {
		return mockMvc.perform(
				post(ACCOUNTS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "%s", "password": "%s"}
								""".formatted(email, password))
		);
	}
}
