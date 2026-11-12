package com.authservice.notification;

public record PasswordChangedNotification(String email) implements Notification {
}
