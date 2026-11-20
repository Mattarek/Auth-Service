package com.authservice.token;

import com.authservice.account.Account;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OneTimeToken {
	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id")
	private Account account;

	@Enumerated(EnumType.STRING)
	private TokenPurpose purpose;
	private String tokenHash;
	private Instant expiresAt;
	private Instant usedAt;
	private Instant createdAt;

	public static OneTimeToken issue(final Account account, final TokenPurpose purpose, final String tokenHash,
									 final Instant now, final Duration validity) {
		Objects.requireNonNull(account, "account must not be null");
		Objects.requireNonNull(purpose, "purpose must not be null");
		Objects.requireNonNull(tokenHash, "tokenHash must not be null");
		Objects.requireNonNull(now, "now must not be null");
		Objects.requireNonNull(validity, "validity must not be null");
		final OneTimeToken token = new OneTimeToken();
		token.account = account;
		token.purpose = purpose;
		token.tokenHash = tokenHash;
		token.createdAt = now;
		token.expiresAt = now.plus(validity);
		return token;
	}

	public boolean isUsed() {
		return usedAt != null;
	}

	public boolean isExpiredAt(final Instant now) {
		return !now.isBefore(expiresAt);
	}

	public void markUsed(final Instant now) {
		Objects.requireNonNull(now, "now must not be null");
		if (isUsed()) {
			throw new IllegalStateException("Token has already been used");
		}
		usedAt = now;
	}
}
