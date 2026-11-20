package com.authservice.account;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	private String email;

	private String passwordHash;

	@Enumerated(EnumType.STRING)
	private AccountStatus status;

	@Enumerated(EnumType.STRING)
	private Role role;

	private Instant passwordChangedAt;

	private int failedLoginAttempts;

	private Instant lockedUntil;

	@Version
	private Long version;
	private Instant createdAt;
	private Instant updatedAt;

	public static Account register(final String email, final String passwordHash, final Instant now) {
		Objects.requireNonNull(email, "email must not be null");
		Objects.requireNonNull(passwordHash, "passwordHash must not be null");
		Objects.requireNonNull(now, "now must not be null");

		final Account account = new Account();
		account.email = email;
		account.passwordHash = passwordHash;
		account.status = AccountStatus.PENDING_ACTIVATION;
		account.role = Role.USER;
		account.passwordChangedAt = now;
		account.createdAt = now;
		account.updatedAt = now;
		return account;
	}

	public void activate(final Instant now) {
		Objects.requireNonNull(now, "now must not be null");
		if (status != AccountStatus.PENDING_ACTIVATION) {
			throw new IllegalStateException("Only an account pending activation can be activated");
		}
		status = AccountStatus.ACTIVE;
		updatedAt = now;
	}
}
