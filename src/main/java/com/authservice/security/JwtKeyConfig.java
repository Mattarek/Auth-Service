package com.authservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

@Slf4j
@Configuration
class JwtKeyConfig {
	private static final int RSA_KEY_SIZE = 2048;

	private static RSAPublicKey readPublicKey(final Resource location) throws IOException {
		try (final InputStream input = location.getInputStream()) {
			return RsaKeyConverters.x509().convert(input);
		}
	}

	private static RSAPrivateKey readPrivateKey(final Resource location) throws IOException {
		try (final InputStream input = location.getInputStream()) {
			return RsaKeyConverters.pkcs8().convert(input);
		}
	}

	@Bean
	@Profile("prod")
	KeyPair jwtKeyPair(final JwtProperties properties) throws IOException {
		final RSAPublicKey publicKey = readPublicKey(
				Objects.requireNonNull(
						properties.publicKeyLocation(), "auth.jwt.public-key-location must be set")
		);
		final RSAPrivateKey privateKey = readPrivateKey(
				Objects.requireNonNull(
						properties.privateKeyLocation(), "auth.jwt.private-key-location must be set")
		);
		return new KeyPair(publicKey, privateKey);
	}

	@Bean
	@Profile("!prod")
	KeyPair ephemeralJwtKeyPair() throws NoSuchAlgorithmException {
		log.warn("Using an ephemeral RSA key pair for JWT signing - tokens will not survive a restart");
		final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(RSA_KEY_SIZE);
		return generator.generateKeyPair();
	}
}
