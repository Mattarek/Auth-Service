package com.authservice.account;

import com.authservice.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class AccountRepositoryIT {
	private static final String EMAIL = "jan@example.com";
	private static final Instant NOW = Instant.parse("2026-11-12T10:00:00Z");

	@Autowired
	private AccountRepository accountRepository;

	@Test
	void shouldPersistNewAccountWithGeneratedUuidV7() {
		// given
		final Account account = Account.register(EMAIL, "hash", NOW);

		// when
		final Account saved = accountRepository.saveAndFlush(account);

		// then
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getId().version()).isEqualTo(7);
		assertThat(saved.getStatus()).isEqualTo(AccountStatus.PENDING_ACTIVATION);
		assertThat(accountRepository.existsByEmail(EMAIL)).isTrue();
	}
}
