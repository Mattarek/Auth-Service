package com.authservice.account;

import java.util.UUID;

public record AccountRegisteredEvent(UUID accountId, String email, String activationToken) {

	@Override
	public String toString() {
		return "AccountRegisteredEvent[accountId=" + accountId + ", email=" + email + ", activationToken=***]";
	}
}
