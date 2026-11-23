package com.authservice.account;

import java.util.UUID;

public record ActivationTokenIssuedEvent(UUID accountId, String email, String activationToken) {

	@Override
	public String toString() {
		return "ActivationTokenIssuedEvent[accountId=" + accountId + ", email=" + email + ", activationToken=***]";
	}
}
