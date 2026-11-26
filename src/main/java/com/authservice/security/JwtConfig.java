package com.authservice.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
class JwtConfig {
	@Bean
	RSAKey jwtSigningKey(final KeyPair jwtKeyPair) throws JOSEException {
		return new RSAKey.Builder((RSAPublicKey) jwtKeyPair.getPublic())
				.privateKey((RSAPrivateKey) jwtKeyPair.getPrivate())
				.keyIDFromThumbprint()
				.build();
	}

	@Bean
	JwtEncoder jwtEncoder(final RSAKey jwtSigningKey) {
		return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwtSigningKey)));
	}

	@Bean
	JwtDecoder jwtDecoder(final RSAKey jwtSigningKey, final JwtProperties properties) throws JOSEException {
		final NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(jwtSigningKey.toRSAPublicKey()).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
		return decoder;
	}
}
