package com.authservice.token;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenGeneratorTest {
	private final TokenGenerator tokenGenerator = new TokenGenerator();

	@Test
	void shouldGenerateUrlSafeTokenOf32RandomBytes() {
		// when
		final GeneratedToken token = tokenGenerator.generate();

		// then
		assertThat(token.rawValue()).hasSize(43).matches("[A-Za-z0-9_-]+");
	}

	@Test
	void shouldStoreSha256HashOfRawValue_whenTokenIsGenerated() {
		// when
		final GeneratedToken token = tokenGenerator.generate();

		// then
		assertThat(token.hash()).isEqualTo(tokenGenerator.hash(token.rawValue()));
	}

	@Test
	void shouldGenerateDifferentTokens_whenCalledTwice() {
		// when
		final GeneratedToken first = tokenGenerator.generate();
		final GeneratedToken second = tokenGenerator.generate();

		// then
		assertThat(first.rawValue()).isNotEqualTo(second.rawValue());
	}

	@Test
	void shouldProduceKnownSha256Hex_whenHashingValue() {
		// given
		final String value = "abc";

		// when
		final String hash = tokenGenerator.hash(value);

		// then
		assertThat(hash).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
	}

	@Test
	void shouldNotExposeRawValue_whenConvertedToString() {
		// given
		final GeneratedToken token = tokenGenerator.generate();

		// when
		final String text = token.toString();

		// then
		assertThat(text).doesNotContain(token.rawValue());
	}
}
