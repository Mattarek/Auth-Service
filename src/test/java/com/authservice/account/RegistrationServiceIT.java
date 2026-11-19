package com.authservice.account;

import com.authservice.TestcontainersConfiguration;
import com.authservice.common.error.BusinessException;
import com.authservice.notification.ActivationLinkNotification;
import com.authservice.notification.MockNotificationClient;
import com.authservice.token.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class RegistrationServiceIT {
	private static final String PASSWORD = "correcthorse1";

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private MockNotificationClient notificationClient;

	@Autowired
	private TokenGenerator tokenGenerator;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void setUp() {
		notificationClient.clear();
	}

	@Test
	void shouldSendActivationLinkWithTokenMatchingStoredHash_whenRegistrationIsCommitted() {
		// given
		final String email = "registration-it@example.com";

		// when
		registrationService.register(email, PASSWORD);

		// then
		assertThat(notificationClient.sentNotifications()).singleElement()
				.isInstanceOfSatisfying(ActivationLinkNotification.class, notification -> {
					assertThat(notification.email()).isEqualTo(email);
					assertThat(countTokensWithHash(tokenGenerator.hash(notification.token()))).isEqualTo(1);
				});
	}

	@Test
	void shouldNotSendActivationLink_whenRegistrationFails() {
		// given
		final String email = "duplicate-it@example.com";
		registrationService.register(email, PASSWORD);
		notificationClient.clear();

		// when
		final BusinessException exception = catchThrowableOfType(BusinessException.class,
				() -> registrationService.register(email.toUpperCase(), PASSWORD));

		// then
		assertThat(exception).isNotNull();
		assertThat(notificationClient.sentNotifications()).isEmpty();
	}

	private Integer countTokensWithHash(final String tokenHash) {
		return jdbcTemplate.queryForObject(
				"select count(*) from one_time_token where token_hash = ?", Integer.class, tokenHash);
	}
}
