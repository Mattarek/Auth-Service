package com.authservice.token;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class TokenGenerator {
	private static final int TOKEN_LENGTH_BYTES = 32;
	private static final String HASH_ALGORITHM = "SHA-256";

	private final SecureRandom secureRandom = new SecureRandom();

	public GeneratedToken generate() {
		final byte[] randomBytes = new byte[TOKEN_LENGTH_BYTES];
		secureRandom.nextBytes(randomBytes);

		final String rawValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
		return new GeneratedToken(rawValue, hash(rawValue));
	}

	public String hash(final String rawValue) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			final byte[] hashBytes = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (final NoSuchAlgorithmException ex) {
			throw new IllegalStateException(HASH_ALGORITHM + " algorithm is not available", ex);
		}
	}
}
