package com.authservice.token;

public record GeneratedToken(String rawValue, String hash) {
	@Override
	public String toString() {
		return "GeneratedToken[rawValue=***, hash=" + hash + "]";
	}
}
