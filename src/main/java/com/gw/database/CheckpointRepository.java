package com.gw.database;

import com.gw.jpa.Checkpoint;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

@Transactional
public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {
  List<Checkpoint> findByWorkflowId(String workflowId);

  List<Checkpoint> findCheckpointByExecutionId(String executionId);

  /**
   * Deletes all workflow checkpoints associated with the specified workflow ID.
   *
   * @param hostid The Workflow ID of the workflow.
   */
  @Modifying
  @Query("delete from Checkpoint wc where wc.workflow.id = ?1")
  void deleteByWorkflowId(String workflowid);

}
