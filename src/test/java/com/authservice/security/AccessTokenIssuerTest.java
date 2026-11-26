package com.authservice.security;

import com.authservice.account.Role;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessTokenIssuerTest {
	private static final String ISSUER = "auth-service";
	private static final Duration VALIDITY = Duration.ofMinutes(15);
	private static final Instant NOW = Instant.parse("2026-11-24T10:00:00Z");
	private static final UUID ACCOUNT_ID = UUID.fromString("0193a3c2-7b1e-7cc0-9b0a-1f2e3d4c5b6a");

	private KeyPair keyPair;
	private AccessTokenIssuer accessTokenIssuer;

	@BeforeEach
	void setUp() throws Exception {
		keyPair = generateKeyPair();
		final RSAKey signingKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate())
				.keyIDFromThumbprint()
				.build();
		accessTokenIssuer = new AccessTokenIssuer(
				new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(signingKey))),
				new JwtProperties(ISSUER, VALIDITY, null, null)
		);
	}

	@Test
	void shouldIssueSignedTokenWithRequiredClaims() {
		// when
		final AccessToken token = accessTokenIssuer.issue(ACCOUNT_ID, Role.USER, NOW);

		// then
		final Jwt jwt = decoderFor(keyPair).decode(token.value());
		assertThat(jwt.getHeaders()).containsEntry("alg", "RS256").containsKey("kid");
		assertThat(jwt.getClaimAsString("iss")).isEqualTo(ISSUER);
		assertThat(jwt.getSubject()).isEqualTo(ACCOUNT_ID.toString());
		assertThat(jwt.getClaimAsStringList("roles")).containsExactly("USER");
		assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
		assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(VALIDITY));
		assertThat(token.expiresAt()).isEqualTo(NOW.plus(VALIDITY));
	}

	@Test
	void shouldBeRejected_whenVerifiedWithDifferentKey() throws Exception {
		// given
		final AccessToken token = accessTokenIssuer.issue(ACCOUNT_ID, Role.USER, NOW);
		final NimbusJwtDecoder foreignDecoder = decoderFor(generateKeyPair());

		// when / then
		assertThatThrownBy(() -> foreignDecoder.decode(token.value()))
				.isInstanceOf(JwtException.class);
	}

	private static NimbusJwtDecoder decoderFor(final KeyPair keyPair) {
		final NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
		decoder.setJwtValidator(new JwtIssuerValidator(ISSUER));
		return decoder;
	}

	private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		return generator.generateKeyPair();
	}
}
