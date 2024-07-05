package com.gw.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "WorkflowCheckpoint")
@Getter
@Setter
@NoArgsConstructor
public class Checkpoint {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Type(uuid-char.class)
  @Column(name = "id", columnDefinition = "VARCHAR(36)")
  private UUID id;

  @Column(name = "executionId")
  private String executionId;

  @Lob
  @Column(name = "edges")
  private String edges;

  @Lob
  @Column(name = "nodes")
  private String nodes;

  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "workflow_id")
  private Workflow workflow;

  @Column(name = "created_at", columnDefinition = "TIMESTAMP")
  private Date createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = new Date();
  }

}
