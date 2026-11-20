package com.authservice.account;

import jakarta.validation.constraints.NotBlank;

public record ActivateAccountRequest(@NotBlank String token) {
	@Override
	public String toString() {
		return "ActivateAccountRequest[token=***]";
	}
}
