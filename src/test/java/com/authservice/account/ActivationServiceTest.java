package com.authservice.account;

import com.authservice.common.error.BusinessException;
import com.authservice.common.error.ErrorCode;
import com.authservice.token.OneTimeToken;
import com.authservice.token.OneTimeTokenRepository;
import com.authservice.token.TokenGenerator;
import com.authservice.token.TokenPurpose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivationServiceTest {
	private static final Instant NOW = Instant.parse("2026-11-20T10:00:00Z");
	private static final String RAW_TOKEN = "raw-token";
	private static final String TOKEN_HASH = "token-hash";
	private static final Duration VALIDITY = Duration.ofHours(24);
	@Mock
	private OneTimeTokenRepository oneTimeTokenRepository;
	@Mock
	private TokenGenerator tokenGenerator;
	private ActivationService activationService;

	@BeforeEach
	void setUp() {
		activationService = new ActivationService(oneTimeTokenRepository, tokenGenerator, Clock.fixed(NOW, ZoneOffset.UTC));
		when(tokenGenerator.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
	}

	@Test
	void shouldActivateAccountAndMarkTokenUsed_whenTokenIsValid() {
		// given
		final OneTimeToken token = givenStoredToken(NOW.minus(Duration.ofHours(1)));
		// when
		activationService.activate(RAW_TOKEN);
		// then
		assertThat(token.getAccount().getStatus()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(token.getUsedAt()).isEqualTo(NOW);
	}

	@Test
	void shouldRejectAsInvalid_whenTokenIsUnknown() {
		// given
		when(oneTimeTokenRepository.findByTokenHashAndPurpose(TOKEN_HASH, TokenPurpose.ACTIVATION))
				.thenReturn(Optional.empty());
		// when
		final BusinessException exception = catchThrowableOfType(BusinessException.class,
				() -> activationService.activate(RAW_TOKEN));
		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_INVALID);
	}

	@Test
	void shouldRejectAsInvalid_whenTokenWasAlreadyUsed() {
		// given
		final OneTimeToken token = givenStoredToken(NOW.minus(Duration.ofHours(1)));
		token.markUsed(NOW.minus(Duration.ofMinutes(30)));
		// when
		final BusinessException exception = catchThrowableOfType(BusinessException.class,
				() -> activationService.activate(RAW_TOKEN));
		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_INVALID);
	}

	@Test
	void shouldRejectAsExpired_whenValidityHasPassed() {
		// given
		final OneTimeToken token = givenStoredToken(NOW.minus(VALIDITY));
		// when
		final BusinessException exception = catchThrowableOfType(BusinessException.class,
				() -> activationService.activate(RAW_TOKEN));
		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED);
		assertThat(token.isUsed()).isFalse();
		assertThat(token.getAccount().getStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
	}

	private OneTimeToken givenStoredToken(final Instant issuedAt) {
		final Account account = Account.register("jan@example.com", "hash", issuedAt);
		final OneTimeToken token = OneTimeToken.issue(account, TokenPurpose.ACTIVATION, TOKEN_HASH, issuedAt, VALIDITY);
		when(oneTimeTokenRepository.findByTokenHashAndPurpose(TOKEN_HASH, TokenPurpose.ACTIVATION))
				.thenReturn(Optional.of(token));
		return token;
	}
}