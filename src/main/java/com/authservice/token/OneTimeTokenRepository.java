package com.authservice.token;

import com.authservice.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OneTimeTokenRepository extends JpaRepository<OneTimeToken, UUID> {
	Optional<OneTimeToken> findByTokenHashAndPurpose(String tokenHash, TokenPurpose purpose);

	@Modifying
	@Query("delete from OneTimeToken t where t.account = :account and t.purpose = :purpose and t.usedAt is null")
	int deleteUnused(Account account, TokenPurpose purpose);
}

