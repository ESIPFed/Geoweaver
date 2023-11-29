package com.gw.jpa;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "checkpoint")
public class Checkpoint {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name="uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Lob
    @Column(name = "edges", columnDefinition = "CLOB")
    private String edges;

    @Lob
    @Column(name = "nodes", columnDefinition = "CLOB")
    private String nodes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private Workflow workflow;

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
