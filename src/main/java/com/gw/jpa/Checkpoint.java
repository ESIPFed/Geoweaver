package com.gw.jpa;

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

  @Column(name = "created_at", columnDefinition = "TIMESTAMP")
  private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

}
