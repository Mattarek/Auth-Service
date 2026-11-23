package com.authservice.account;

import com.authservice.token.GeneratedToken;
import com.authservice.token.OneTimeToken;
import com.authservice.token.OneTimeTokenRepository;
import com.authservice.token.TokenGenerator;
import com.authservice.token.TokenProperties;
import com.authservice.token.TokenPurpose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivationTokenIssuerTest {
	private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");
	private static final Duration ACTIVATION_VALIDITY = Duration.ofHours(24);

	@Mock
	private OneTimeTokenRepository oneTimeTokenRepository;

	@Mock
	private TokenGenerator tokenGenerator;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private ActivationTokenIssuer activationTokenIssuer;

	private final Account account = Account.register("jan@example.com", "hash", NOW);

	@BeforeEach
	void setUp() {
		activationTokenIssuer = new ActivationTokenIssuer(
				oneTimeTokenRepository,
				tokenGenerator,
				new TokenProperties(ACTIVATION_VALIDITY, Duration.ofMinutes(30)),
				eventPublisher
		);
		when(tokenGenerator.generate()).thenReturn(new GeneratedToken("raw-token", "token-hash"));
	}

	@Test
	void shouldInvalidatePreviousTokensBeforeSavingNewOne() {
		// when
		activationTokenIssuer.issue(account, NOW);

		// then
		final InOrder order = inOrder(oneTimeTokenRepository);
		order.verify(oneTimeTokenRepository).deleteUnused(account, TokenPurpose.ACTIVATION);
		order.verify(oneTimeTokenRepository).save(any(OneTimeToken.class));
	}

	@Test
	void shouldStoreOnlyHashOfActivationToken() {
		// given
		final ArgumentCaptor<OneTimeToken> tokenCaptor = ArgumentCaptor.forClass(OneTimeToken.class);

		// when
		activationTokenIssuer.issue(account, NOW);

		// then
		verify(oneTimeTokenRepository).save(tokenCaptor.capture());
		final OneTimeToken token = tokenCaptor.getValue();
		assertThat(token.getPurpose()).isEqualTo(TokenPurpose.ACTIVATION);
		assertThat(token.getTokenHash()).isEqualTo("token-hash");
		assertThat(token.getExpiresAt()).isEqualTo(NOW.plus(ACTIVATION_VALIDITY));
	}

	@Test
	void shouldPublishEventWithRawToken() {
		// given
		final ArgumentCaptor<ActivationTokenIssuedEvent> eventCaptor =
				ArgumentCaptor.forClass(ActivationTokenIssuedEvent.class);

		// when
		activationTokenIssuer.issue(account, NOW);

		// then
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().email()).isEqualTo("jan@example.com");
		assertThat(eventCaptor.getValue().activationToken()).isEqualTo("raw-token");
	}
}
