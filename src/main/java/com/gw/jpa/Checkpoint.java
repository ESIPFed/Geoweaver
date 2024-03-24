package com.gw.jpa;

import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "WorkflowCheckpoint")
public class Checkpoint {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Type(type = "uuid-char")
  @Column(name = "id", columnDefinition = "VARCHAR(36)")
  private UUID id;

  @Column(name = "executionId")
  private String executionId;

  @Lob
  @Column(name = "edges", columnDefinition = "CLOB")
  private String edges;

  @Lob
  @Column(name = "nodes", columnDefinition = "CLOB")
  private String nodes;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "workflow_id")
  private Workflow workflow;

  @Column(name = "created_at")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = new Date();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEdges() {
    return edges;
  }

  public void setEdges(String edges) {
    this.edges = edges;
  }

  public String getNodes() {
    return nodes;
  }

  public void setNodes(String nodes) {
    this.nodes = nodes;
  }

  public Workflow getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Workflow workflow) {
    this.workflow = workflow;
  }
}
