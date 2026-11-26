package com.authservice.security;

import com.authservice.account.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccessTokenIssuer {
	static final String ROLES_CLAIM = "roles";

	private final JwtEncoder jwtEncoder;
	private final JwtProperties properties;

	public AccessToken issue(final UUID accountId, final Role role, final Instant now) {
		final Instant expiresAt = now.plus(properties.accessTokenValidity());
		final JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.issuer())
				.subject(accountId.toString())
				.issuedAt(now)
				.expiresAt(expiresAt)
				.claim(ROLES_CLAIM, List.of(role.name()))
				.build();
		final JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
		final String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new AccessToken(value, expiresAt);
	}
}
