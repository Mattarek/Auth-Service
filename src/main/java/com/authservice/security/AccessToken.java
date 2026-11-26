package com.authservice.security;

import java.time.Instant;

public record AccessToken(String value, Instant expiresAt) {
	@Override
	public String toString() {
		return "AccessToken[value=***, expiresAt=" + expiresAt + "]";
	}
}
