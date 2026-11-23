package com.authservice.account;

import com.authservice.common.error.BusinessException;
import com.authservice.common.error.ErrorCode;
import com.authservice.common.error.FieldValidationException;
import com.authservice.common.error.FieldViolation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {
	private static final String PASSWORD_FIELD = "password";
	private final AccountRepository accountRepository;
	private final PasswordPolicy passwordPolicy;
	private final PasswordEncoder passwordEncoder;
	private final ActivationTokenIssuer activationTokenIssuer;
	private final Clock clock;

	@Transactional
	public Account register(final String rawEmail, final String rawPassword) {
		final String email = EmailNormalizer.normalize(rawEmail);
		validatePassword(rawPassword, email);
		if (accountRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_TAKEN);
		}
		final Instant now = Instant.now(clock);
		final Account account = saveAccount(Account.register(email, passwordEncoder.encode(rawPassword), now));
		activationTokenIssuer.issue(account, now);
		return account;
	}

	private void validatePassword(final String rawPassword, final String email) {
		final List<FieldViolation> violations = passwordPolicy.validate(rawPassword, email)
				.stream()
				.map(message -> new FieldViolation(PASSWORD_FIELD, message))
				.toList();
		if (!violations.isEmpty()) {
			throw new FieldValidationException(violations);
		}
	}

	private Account saveAccount(final Account account) {
		try {
			return accountRepository.saveAndFlush(account);
		} catch (final DataIntegrityViolationException ex) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_TAKEN);
		}
	}
}