package com.authservice.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@ConditionalOnProperty(name = "auth.notifications.client", havingValue = "mock")
public class MockNotificationClient implements NotificationClient {
	private final List<Notification> sentNotifications = new CopyOnWriteArrayList<>();
	private final boolean logTokens;

	public MockNotificationClient(@Value("${auth.notifications.mock.log-tokens:false}") final boolean logTokens) {
		this.logTokens = logTokens;
	}

	private static String describeWithToken(final Notification notification) {
		return switch (notification) {
			case final ActivationLinkNotification n ->
					"activation link for %s, token=%s".formatted(n.email(), n.token());
			case final PasswordResetLinkNotification n ->
					"password reset link for %s, token=%s".formatted(n.email(), n.token());
			case final PasswordChangedNotification n -> "password changed notice for %s".formatted(n.email());
		};
	}

	@Override
	public void send(final Notification notification) {
		sentNotifications.add(notification);
		log.info("Notification ordered: {}", logTokens ? describeWithToken(notification) : notification);
	}

	public List<Notification> sentNotifications() {
		return List.copyOf(sentNotifications);
	}

	public void clear() {
		sentNotifications.clear();
	}
}
