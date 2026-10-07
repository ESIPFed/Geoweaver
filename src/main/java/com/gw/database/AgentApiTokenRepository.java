package com.gw.database;

import com.gw.jpa.AgentApiToken;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

@Transactional
public interface AgentApiTokenRepository extends CrudRepository<AgentApiToken, String> {

  Optional<AgentApiToken> findByTokenHash(String tokenHash);

  Optional<AgentApiToken> findByFingerprint(String fingerprint);

  List<AgentApiToken> findByRevokedAtIsNull();

  long countByRevokedAtIsNullAndExpiresAtAfter(Instant now);
}
