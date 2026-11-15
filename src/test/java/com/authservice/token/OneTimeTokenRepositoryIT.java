package com.authservice.token;

import com.authservice.TestcontainersConfiguration;
import com.authservice.account.Account;
import com.authservice.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class OneTimeTokenRepositoryIT {
	private static final Instant NOW = Instant.parse("2026-11-14T10:00:00Z");

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private OneTimeTokenRepository oneTimeTokenRepository;

	@Test
	void shouldPersistTokenLinkedToAccount() {
		// given
		final Account account = accountRepository.saveAndFlush(
				Account.register(
						"token-owner@example.com",
						"hash",
						NOW
				));

		final OneTimeToken token = OneTimeToken.issue(
				account,
				TokenPurpose.ACTIVATION,
				"a".repeat(64),
				NOW,
				Duration.ofHours(24)
		);

		// when
		final OneTimeToken saved = oneTimeTokenRepository.saveAndFlush(token);

		// then
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
		assertThat(saved.getUsedAt()).isNull();
	}
}
