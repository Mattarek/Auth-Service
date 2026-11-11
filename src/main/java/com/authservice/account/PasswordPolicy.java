package com.authservice.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PasswordPolicy {
	private final PasswordPolicyProperties properties;

	public List<String> validate(final String password, final String email) {
		final List<String> violations = new ArrayList<>();

		if (password.length() < properties.minLength() || password.length() > properties.maxLength()) {
			violations.add("must be between %d and %d characters".formatted(properties.minLength(),
					properties.maxLength()));
		}
		if (properties.requireLetter() && password.chars().noneMatch(Character::isLetter)) {
			violations.add("must contain at least one letter");
		}
		if (properties.requireDigit() && password.chars().noneMatch(Character::isDigit)) {
			violations.add("must contain at least one digit");
		}
		if (password.equalsIgnoreCase(email)) {
			violations.add("must not be the same as email");
		}
		return violations;
	}
}
