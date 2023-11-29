package com.gw.database;

import com.gw.jpa.Checkpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {
    List<Checkpoint> findByWorkflowId(String workflowId);
}
