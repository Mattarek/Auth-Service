package com.authservice.account;

import java.util.Locale;

public final class EmailNormalizer {
	private EmailNormalizer() {
	}

	public static String normalize(final String email) {
		return email.strip().toLowerCase(Locale.ROOT);
	}
}
