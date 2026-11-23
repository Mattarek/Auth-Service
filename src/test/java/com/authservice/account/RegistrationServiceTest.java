package com.authservice.account;

import com.authservice.common.error.BusinessException;
import com.authservice.common.error.ErrorCode;
import com.authservice.common.error.FieldValidationException;
import com.authservice.common.error.FieldViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
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

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private PasswordPolicy passwordPolicy;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ActivationTokenIssuer activationTokenIssuer;

	private RegistrationService registrationService;

	@BeforeEach
	void setUp() {
		registrationService = new RegistrationService(
				accountRepository,
				passwordPolicy,
				passwordEncoder,
				activationTokenIssuer,
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
	void shouldIssueActivationToken_whenAccountIsRegistered() {
		// given
		givenValidRegistration();

		// when
		final Account account = registrationService.register(EMAIL, PASSWORD);

		// then
		verify(activationTokenIssuer).issue(account, NOW);
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
		verifyNoInteractions(activationTokenIssuer);
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
		verifyNoInteractions(accountRepository, activationTokenIssuer);
	}

	private void givenValidRegistration() {
		when(passwordPolicy.validate(PASSWORD, EMAIL)).thenReturn(List.of());
		when(accountRepository.existsByEmail(EMAIL)).thenReturn(false);
		when(passwordEncoder.encode(PASSWORD)).thenReturn("hashed-password");
		when(accountRepository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}
}
