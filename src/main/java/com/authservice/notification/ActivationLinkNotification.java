package com.authservice.notification;

public record ActivationLinkNotification(String email, String token) implements Notification {
	@Override
	public String toString() {
		return "ActivationLinkNotification[email=" + email + ", token=***]";
	}
}
