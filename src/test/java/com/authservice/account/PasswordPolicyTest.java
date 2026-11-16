package com.authservice.account;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTest {
	private static final String EMAIL = "jan@example.com";
	private static final String LENGTH_VIOLATION = "must be between 12 and 128 characters";

	private final PasswordPolicy policy = new PasswordPolicy(new PasswordPolicyProperties(12, 128, true, true));

	@Test
	void shouldAcceptPassword_whenAllRulesAreMet() {
		// given
		final String password = "correcthorse1";

		// when
		final List<String> violations = policy.validate(password, EMAIL);

		// then
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldRejectPassword_whenShorterThanMinLength() {
		// given
		final String password = "short1";

		// when
		final List<String> violations = policy.validate(password, EMAIL);

		// then
		assertThat(violations).containsExactly(LENGTH_VIOLATION);
	}

	@Test
	void shouldRejectPassword_whenLongerThanMaxLength() {
		// given
		final String password = "a1".repeat(65);

		// when
		final List<String> violations = policy.validate(password, EMAIL);

		// then
		assertThat(violations).containsExactly(LENGTH_VIOLATION);
	}

	@Test
	void shouldRejectPassword_whenItHasNoDigit() {
		// given
		final String password = "onlyletterspassword";

		// when
		final List<String> violations = policy.validate(password, EMAIL);

		// then
		assertThat(violations).containsExactly("must contain at least one digit");
	}

	@Test
	void shouldRejectPassword_whenItHasNoLetter() {
		// given
		final String password = "123456789012";

		// when
		final List<String> violations = policy.validate(password, EMAIL);

		// then
		assertThat(violations).containsExactly("must contain at least one letter");
	}

	@Test
	void shouldRejectPassword_whenEqualToEmailIgnoringCase() {
		// given
		final String email = "jan1@example.com";
		final String password = "JAN1@example.com";

		// when
		final List<String> violations = policy.validate(password, email);

		// then
		assertThat(violations).containsExactly("must not be the same as email");
	}

	@Test
	void shouldReportAllViolations_whenSeveralRulesAreBroken() {
		// given
		final String password = "abc";

		// when
		final List<String> violations = policy.validate(password, EMAIL);

		// then
		assertThat(violations).containsExactlyInAnyOrder(LENGTH_VIOLATION, "must contain at least one digit");
	}

	@Test
	void shouldAcceptPasswordWithoutDigit_whenDigitRuleIsDisabled() {
		// given
		final PasswordPolicy lenientPolicy = new PasswordPolicy(new PasswordPolicyProperties(12, 128, true, false));
		final String password = "onlyletterspassword";

		// when
		final List<String> violations = lenientPolicy.validate(password, EMAIL);

		// then
		assertThat(violations).isEmpty();
	}
}
