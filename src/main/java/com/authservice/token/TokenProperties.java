package com.authservice.token;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "auth.tokens")
public record TokenProperties(
		@NotNull Duration activationValidity,
		@NotNull Duration passwordResetValidity
) {
}