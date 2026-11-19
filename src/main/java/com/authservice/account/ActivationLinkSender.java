package com.authservice.account;

import com.authservice.notification.ActivationLinkNotification;
import com.authservice.notification.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
class ActivationLinkSender {
	private final NotificationClient notificationClient;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void onAccountRegistered(final AccountRegisteredEvent accountRegisteredEvent) {
		try {
			notificationClient.send(new ActivationLinkNotification(
					accountRegisteredEvent.email(),
					accountRegisteredEvent.activationToken()));
		} catch (final RuntimeException ex) {
			log.error("Failed to send activation link for account {}", accountRegisteredEvent.accountId(), ex);
		}
	}
}
