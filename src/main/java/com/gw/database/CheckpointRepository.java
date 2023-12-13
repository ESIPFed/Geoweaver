package com.gw.database;

import com.gw.jpa.Checkpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {
    List<Checkpoint> findByWorkflowId(String workflowId);

    List<Checkpoint> findCheckpointByExecutionId(String executionId);
}
