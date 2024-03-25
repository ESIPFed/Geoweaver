package com.gw.database;

import com.gw.jpa.Checkpoint;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {
  List<Checkpoint> findByWorkflowId(String workflowId);

  List<Checkpoint> findCheckpointByExecutionId(String executionId);
}
