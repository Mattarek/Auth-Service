package com.authservice.account;

import com.authservice.TestcontainersConfiguration;
import com.authservice.notification.ActivationLinkNotification;
import com.authservice.notification.MockNotificationClient;
import com.authservice.token.OneTimeToken;
import com.authservice.token.OneTimeTokenRepository;
import com.authservice.token.TokenGenerator;
import com.authservice.token.TokenPurpose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AccountActivationIT {
	private static final String PASSWORD = "correcthorse1";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private OneTimeTokenRepository oneTimeTokenRepository;

	@Autowired
	private TokenGenerator tokenGenerator;

	@Autowired
	private MockNotificationClient notificationClient;

	@BeforeEach
	void setUp() {
		notificationClient.clear();
	}

	@Test
	void shouldActivateAccount_whenTokenFromActivationEmailIsSent() throws Exception {
		// given
		final Account account = registrationService.register("activate-it@example.com", PASSWORD);
		final String token = sentActivationToken();

		// when
		final ResultActions result = activate(token);

		// then
		result.andExpect(status().isNoContent());
		assertThat(accountRepository.findById(account.getId()))
				.hasValueSatisfying(saved -> assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE));
	}

	@Test
	void shouldRejectAsInvalid_whenTokenIsReused() throws Exception {
		// given
		registrationService.register("reuse-it@example.com", PASSWORD);
		final String token = sentActivationToken();
		activate(token).andExpect(status().isNoContent());

		// when
		final ResultActions result = activate(token);

		// then
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
	}

	@Test
	void shouldRejectAsExpired_whenTokenIsPastItsValidity() throws Exception {
		// given
		final Account account = registrationService.register("expired-it@example.com", PASSWORD);
		final var expiredToken = tokenGenerator.generate();
		final Instant issuedAt = Instant.now().minus(Duration.ofHours(25));
		oneTimeTokenRepository.save(OneTimeToken.issue(
				account, TokenPurpose.ACTIVATION, expiredToken.hash(), issuedAt, Duration.ofHours(24)));

		// when
		final ResultActions result = activate(expiredToken.rawValue());

		// then
		result.andExpect(status().isGone())
				.andExpect(jsonPath("$.code").value("TOKEN_EXPIRED"));
	}

	@Test
	void shouldSendNewLinkAndInvalidatePreviousToken_whenResendIsRequested() throws Exception {
		// given
		registrationService.register("resend-it@example.com", PASSWORD);
		final String previousToken = sentActivationToken();
		notificationClient.clear();

		// when
		final ResultActions result = resend("resend-it@example.com");

		// then
		result.andExpect(status().isAccepted());
		final String newToken = sentActivationToken();
		activate(previousToken).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("TOKEN_INVALID"));
		activate(newToken).andExpect(status().isNoContent());
	}

	@Test
	void shouldReturnSameResponseWithoutSendingAnything_whenEmailIsUnknown() throws Exception {

		// when
		final ResultActions result = resend("nobody-it@example.com");

		// then
		result.andExpect(status().isAccepted());
		assertThat(notificationClient.sentNotifications()).isEmpty();
	}

	private ResultActions resend(final String email) throws Exception {
		return mockMvc.perform(post("/api/v1/accounts/activation/resend")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s"}
						""".formatted(email)));
	}

	private String sentActivationToken() {
		return notificationClient.sentNotifications().stream()
				.map(ActivationLinkNotification.class::cast)
				.findFirst()
				.orElseThrow()
				.token();
	}

	private ResultActions activate(final String token) throws Exception {
		return mockMvc.perform(post("/api/v1/accounts/activation")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"token": "%s"}
						""".formatted(token)));
	}
}
