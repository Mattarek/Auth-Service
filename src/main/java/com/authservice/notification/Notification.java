package com.authservice.notification;

public sealed interface Notification permits
		ActivationLinkNotification,
		PasswordResetLinkNotification,
		PasswordChangedNotification {
	String email();
}
