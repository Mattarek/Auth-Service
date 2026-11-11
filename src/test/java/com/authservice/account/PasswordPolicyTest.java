package com.authservice.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordPolicyTest {
	private static final String EMAIL = "jan@example.com";
	private final PasswordPolicy policy = new PasswordPolicy(new PasswordPolicyProperties(12, 128, true, true));

	@Test
	void shouldAcceptValidPassword() {
		assertThat(policy.validate("correcthorse1", EMAIL)).isEmpty();
	}

	@Test
	void shouldRejectTooShortPassword() {
		assertThat(policy.validate("short1", EMAIL)).containsExactly("must be between 12 and 128 characters");
	}

	@Test
	void shouldRejectTooLongPassword() {
		assertThat(policy.validate("a1".repeat(65), EMAIL)).containsExactly("must be between 12 and 128 characters");
	}

	@Test
	void shouldRequireDigit() {
		assertThat(policy.validate("onlyletterspassword", EMAIL)).containsExactly("must contain at least one digit");
	}

	@Test
	void shouldRequireLetter() {
		assertThat(policy.validate("123456789012", EMAIL)).containsExactly("must contain at least one letter");
	}

	@Test
	void shouldRejectPasswordEqualToEmailIgnoringCase() {
		assertThat(policy.validate("JAN1@example.com", "jan1@example.com")).containsExactly("must not be the same as email");
	}

	@Test
	void shouldReportAllViolationsAtOnce() {
		assertThat(policy.validate("abc", EMAIL)).hasSize(2);
	}

	@Test
	void shouldSkipDigitRuleWhenDisabled() {
		final PasswordPolicy lenient = new PasswordPolicy(new PasswordPolicyProperties(12, 128, true, false));
		assertThat(lenient.validate("onlyletterspassword", EMAIL)).isEmpty();
	}
}