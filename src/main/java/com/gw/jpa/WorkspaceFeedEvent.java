package com.gw.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Data;

/**
 * A change notice shared through the database between Geoweaver processes that use the same
 * database (for example a server in an Open OnDemand session and another one an agent drives over
 * SSH), so every open browser sees what the others changed. Rows are kept for an hour.
 */
@Entity
@Data
@Table(
    name = "gw_workspace_feed",
    indexes = {@Index(name = "idx_workspace_feed_created", columnList = "created_at")})
public class WorkspaceFeedEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  /** Random id of the Geoweaver process that wrote the row. */
  @Column(length = 64, nullable = false)
  private String origin;

  /** The event as JSON (no code, no secrets). */
  @Column(length = 4000, nullable = false)
  private String payload;
}
