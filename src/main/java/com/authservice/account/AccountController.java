package com.authservice.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
class AccountController {
	private final RegistrationService registrationService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	AccountResponse register(@Valid @RequestBody final RegisterAccountRequest request) {
		return AccountResponse.from(registrationService.register(request.email(), request.password()));
	}
}
