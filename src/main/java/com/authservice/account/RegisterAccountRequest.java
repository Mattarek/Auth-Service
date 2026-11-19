package com.authservice.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterAccountRequest(
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank String password
) {
	@Override
	public String toString() {
		return "RegisterAccountRequest[email=" + email + "]";
	}
}
