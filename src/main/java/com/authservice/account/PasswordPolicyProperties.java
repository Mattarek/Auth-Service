package com.authservice.account;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.password-policy")
public record PasswordPolicyProperties(
		int minLength,
		int maxLength,
		boolean requireLetter,
		boolean requireDigit
) {
}
