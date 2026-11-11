package com.authservice.account;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth.password-policy")
public record PasswordPolicyProperties(
		@Min(12) int minLength,
		@Max(128) int maxLength,
		boolean requireLetter,
		boolean requireDigit
) {
}
