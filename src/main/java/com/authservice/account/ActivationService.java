package com.authservice.account;

import com.authservice.common.error.BusinessException;
import com.authservice.common.error.ErrorCode;
import com.authservice.token.OneTimeToken;
import com.authservice.token.OneTimeTokenRepository;
import com.authservice.token.TokenGenerator;
import com.authservice.token.TokenPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ActivationService {
	private final OneTimeTokenRepository oneTimeTokenRepository;
	private final TokenGenerator tokenGenerator;
	private final Clock clock;

	@Transactional
	public void activate(final String rawToken) {
		final Instant now = Instant.now(clock);
		final OneTimeToken oneTimeToken =
				oneTimeTokenRepository.findByTokenHashAndPurpose(tokenGenerator.hash(rawToken), TokenPurpose.ACTIVATION)
						.filter(found -> !found.isUsed())
						.orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

		if (oneTimeToken.isExpiredAt(now)) {
			throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
		}

		oneTimeToken.markUsed(now);
		oneTimeToken.getAccount().activate(now);
	}
}
