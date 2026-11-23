package com.authservice.account;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {
	private static final Instant NOW = Instant.parse("2026-11-24T10:00:00Z");
	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

	private static Account newAccount() {
		return Account.register("jan@example.com", "hash", NOW);
	}

	private static void failLogin(final Account account, final int times) {
		for (int attempt = 0; attempt < times; attempt++) {
			account.registerFailedLogin(NOW, MAX_FAILED_ATTEMPTS, LOCK_DURATION);
		}
	}

	@Test
	void shouldNotBeLocked_whenAccountIsNew() {
		// given
		final Account account = newAccount();
		// when
		final boolean locked = account.isLockedAt(NOW);
		// then
		assertThat(locked).isFalse();
	}

	@Test
	void shouldNotLock_whenFailedAttemptsAreBelowLimit() {
		// given
		final Account account = newAccount();
		// when
		failLogin(account, MAX_FAILED_ATTEMPTS - 1);
		// then
		assertThat(account.isLockedAt(NOW)).isFalse();
		assertThat(account.getFailedLoginAttempts()).isEqualTo(MAX_FAILED_ATTEMPTS - 1);
	}

	@Test
	void shouldLockForConfiguredDuration_whenFailedAttemptsReachLimit() {
		// given
		final Account account = newAccount();
		// when
		failLogin(account, MAX_FAILED_ATTEMPTS);
		// then
		assertThat(account.isLockedAt(NOW)).isTrue();
		assertThat(account.getLockedUntil()).isEqualTo(NOW.plus(LOCK_DURATION));
		assertThat(account.getFailedLoginAttempts()).isZero();
	}

	@Test
	void shouldUnlock_whenLockDurationHasPassed() {
		// given
		final Account account = newAccount();
		failLogin(account, MAX_FAILED_ATTEMPTS);
		// when
		final boolean locked = account.isLockedAt(NOW.plus(LOCK_DURATION));
		// then
		assertThat(locked).isFalse();
	}

	@Test
	void shouldResetFailedAttempts_whenLoginSucceeds() {
		// given
		final Account account = newAccount();
		failLogin(account, MAX_FAILED_ATTEMPTS - 1);
		// when
		account.registerSuccessfulLogin(NOW);
		// then
		assertThat(account.getFailedLoginAttempts()).isZero();
		failLogin(account, MAX_FAILED_ATTEMPTS - 1);
		assertThat(account.isLockedAt(NOW)).isFalse();
	}
}
