package com.authservice.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockNotificationClientTest {
	private static final String EMAIL = "jan@example.com";

	private final MockNotificationClient client = new MockNotificationClient(false);

	@Test
	void shouldRecordNotification_whenSent() {
		// given
		final Notification notification = new ActivationLinkNotification(EMAIL, "token-123");

		// when
		client.send(notification);

		// then
		assertThat(client.sentNotifications()).containsExactly(notification);
	}

	@Test
	void shouldForgetRecordedNotifications_whenCleared() {
		// given
		client.send(new PasswordChangedNotification(EMAIL));

		// when
		client.clear();

		// then
		assertThat(client.sentNotifications()).isEmpty();
	}

	@Test
	void shouldMaskToken_whenNotificationIsConvertedToString() {
		// given
		final Notification notification = new PasswordResetLinkNotification(EMAIL, "secret-token");

		// when
		final String text = notification.toString();

		// then
		assertThat(text).doesNotContain("secret-token");
	}
}
