package com.authservice.account;

import java.util.UUID;

public record AccountResponse(UUID id, String email, AccountStatus status) {
	static AccountResponse from(final Account account) {
		return new AccountResponse(account.getId(), account.getEmail(), account.getStatus());
	}
}
