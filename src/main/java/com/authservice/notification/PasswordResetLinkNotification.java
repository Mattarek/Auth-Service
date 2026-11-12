package com.authservice.notification;

public record PasswordResetLinkNotification(String email, String token) implements Notification {
	@Override
	public String toString() {
		return "PasswordResetLinkNotification[email=" + email + ", token=***]";
	}
}
