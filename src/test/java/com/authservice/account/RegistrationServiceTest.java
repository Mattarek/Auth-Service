package com.authservice.account;

import com.authservice.common.error.BusinessException;
import com.authservice.common.error.ErrorCode;
import com.authservice.common.error.FieldValidationException;
import com.authservice.common.error.FieldViolation;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {
	private static final Instant NOW = Instant.parse("2026-11-15T10:00:00Z");
	private static final String EMAIL = "jan@example.com";
	private static final String PASSWORD = "correctpassword1";
	private static final Duration ACTIVATION_VALIDITY = Duration.ofHours(24);

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private OneTimeTokenRepository oneTimeTokenRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private PasswordPolicy passwordPolicy;

	@Mock
	private TokenGenerator tokenGenerator;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private RegistrationService registrationService;

	@BeforeEach
	void setUp() {
		registrationService = new RegistrationService(
				accountRepository,
				oneTimeTokenRepository,
				passwordPolicy,
				passwordEncoder,
				tokenGenerator,
				new TokenProperties(ACTIVATION_VALIDITY, Duration.ofMinutes(30)),
				eventPublisher,
				Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	@Test
	void shouldRegisterPendingAccountWithNormalizedEmailAndHashedPassword() {
		// given
		givenValidRegistration();

		// when
		final Account account = registrationService.register("  Jan@Example.COM ", PASSWORD);

		// then
		assertThat(account.getEmail()).isEqualTo(EMAIL);
		assertThat(account.getPasswordHash()).isEqualTo("hashed-password");
		assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
		assertThat(account.getCreatedAt()).isEqualTo(NOW);
	}

	@Test
	void shouldStoreOnlyHashOfActivationToken_whenAccountIsRegistered() {
		// given
		givenValidRegistration();
		final ArgumentCaptor<OneTimeToken> tokenCaptor = ArgumentCaptor.forClass(OneTimeToken.class);

		// when
		registrationService.register(EMAIL, PASSWORD);

		// then
		verify(oneTimeTokenRepository).save(tokenCaptor.capture());
		final OneTimeToken token = tokenCaptor.getValue();
		assertThat(token.getPurpose()).isEqualTo(TokenPurpose.ACTIVATION);
		assertThat(token.getTokenHash()).isEqualTo("token-hash");
		assertThat(token.getExpiresAt()).isEqualTo(NOW.plus(ACTIVATION_VALIDITY));
	}

	@Test
	void shouldPublishEventWithRawActivationToken_whenAccountIsRegistered() {
		// given
		givenValidRegistration();
		final ArgumentCaptor<AccountRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(AccountRegisteredEvent.class);

		// when
		registrationService.register(EMAIL, PASSWORD);

		// then
		verify(eventPublisher).publishEvent(eventCaptor.capture());
		assertThat(eventCaptor.getValue().email()).isEqualTo(EMAIL);
		assertThat(eventCaptor.getValue().activationToken()).isEqualTo("raw-token");
	}

	@Test
	void shouldRejectRegistration_whenEmailIsAlreadyTaken() {
		// given
		when(passwordPolicy.validate(PASSWORD, EMAIL)).thenReturn(List.of());
		when(accountRepository.existsByEmail(EMAIL)).thenReturn(true);

		// when
		final BusinessException exception = catchThrowableOfType(BusinessException.class,
				() -> registrationService.register("JAN@example.com", PASSWORD));

		// then
		assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_TAKEN);
		verify(accountRepository, never()).saveAndFlush(any());
		verifyNoInteractions(oneTimeTokenRepository, eventPublisher);
	}

	@Test
	void shouldRejectRegistrationWithPasswordFieldErrors_whenPasswordViolatesPolicy() {
		// given
		when(passwordPolicy.validate("short", EMAIL)).thenReturn(List.of("must be between 12 and 128 characters"));

		// when
		final FieldValidationException exception = catchThrowableOfType(FieldValidationException.class,
				() -> registrationService.register(EMAIL, "short"));

		// then
		assertThat(exception.getViolations())
				.containsExactly(new FieldViolation("password", "must be between 12 and 128 characters"));
		verifyNoInteractions(accountRepository, oneTimeTokenRepository, eventPublisher);
	}

	private void givenValidRegistration() {
		when(passwordPolicy.validate(PASSWORD, EMAIL)).thenReturn(List.of());
		when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
		when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed-password");
		when(accountRepository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(tokenGenerator.generate()).thenReturn(new GeneratedToken("raw-token", "token-hash"));
	}
}
