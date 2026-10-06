package com.gw.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Agent API Bearer token metadata. The raw secret is never stored — only a SHA-256 hash. */
@Entity
@Table(name = "agent_api_token")
@Getter
@Setter
@NoArgsConstructor
public class AgentApiToken {

  @Id
  @Column(length = 32)
  private String id;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(nullable = false, unique = true, length = 16)
  private String fingerprint;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant revokedAt;

  @Column(nullable = false)
  private Integer ttlDays;
}
