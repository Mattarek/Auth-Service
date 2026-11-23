package com.authservice.account;

import com.authservice.token.GeneratedToken;
import com.authservice.token.OneTimeToken;
import com.authservice.token.OneTimeTokenRepository;
import com.authservice.token.TokenGenerator;
import com.authservice.token.TokenProperties;
import com.authservice.token.TokenPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AcctivationTokenIssuer {
	private final OneTimeTokenRepository oneTimeTokenRepository;
	private final TokenGenerator tokenGenerator;
	private final TokenProperties tokenProperties;
	private final ApplicationEventPublisher applicationEventPublisher;

	void issue(final Account account, final Instant now) {
		oneTimeTokenRepository.deleteUnused(account, TokenPurpose.ACTIVATION);
		final GeneratedToken activationToken = tokenGenerator.generate();

		oneTimeTokenRepository.save(OneTimeToken.issue(
				account,
				TokenPurpose.ACTIVATION,
				activationToken.hash(),
				now,
				tokenProperties.activationValidity()
		));

		applicationEventPublisher.publishEvent(
				new ActivationTokenIssuedEvent(account.getId(), account.getEmail(), activationToken.rawValue())
		);
	}
}
