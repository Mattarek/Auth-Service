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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
	private final OneTimeTokenRepository oneTimeTokenRepository;
	private final PasswordPolicy passwordPolicy;
	private final PasswordEncoder passwordEncoder;
	private final TokenGenerator tokenGenerator;
	private final TokenProperties tokenProperties;
	private final ApplicationEventPublisher eventPublisher;
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
		final GeneratedToken activationToken = tokenGenerator.generate();
		oneTimeTokenRepository.save(OneTimeToken.issue(
				account,
				TokenPurpose.ACTIVATION,
				activationToken.hash(),
				now,
				tokenProperties.activationValidity()
		));
		eventPublisher.publishEvent(new AccountRegisteredEvent(account.getId(), email, activationToken.rawValue()));
		return account;
	}

	private void validatePassword(final String rawPassword, final String email) {
		final List<FieldViolation> violations = passwordPolicy.validate(rawPassword, email).stream()
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