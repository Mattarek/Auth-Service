package com.authservice.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockNotificationClientTest {
	private final MockNotificationClient client = new MockNotificationClient(false);

	@Test
	void shouldRecordSentNotifications() {
		final var notification = new ActivationLinkNotification("jan@example.com", "token-123");
		client.send(notification);
		assertThat(client.sentNotifications()).containsExactly(notification);
	}

	@Test
	void shouldClearRecordedNotifications() {
		client.send(new PasswordChangedNotification("jan@example.com"));
		client.clear();
		assertThat(client.sentNotifications()).isEmpty();
	}

	@Test
	void shouldMaskTokenInToString() {
		final var notification = new PasswordResetLinkNotification("jan@example.com", "secret-token");
		assertThat(notification.toString()).doesNotContain("secret-token");
	}
}