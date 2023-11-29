package com.gw.tools;

import com.gw.database.CheckpointRepository;
import com.gw.database.WorkflowRepository;
import com.gw.dto.checkpoint.CheckpointCreateRequest;
import com.gw.jpa.Checkpoint;
import com.gw.jpa.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Scope("prototype")
public class CheckpointTool {

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    public List<Checkpoint> getCheckpointByWorkflowId(String workflowId) {
        return checkpointRepository.findByWorkflowId(workflowId);
    }

    public Checkpoint createCheckpoint(CheckpointCreateRequest createRequest) {
        String workflowId = createRequest.getWorkflowId();
        Optional<Workflow> optionalWorkflow = workflowRepository.findById(workflowId);
        if (optionalWorkflow.isPresent()) {
            Workflow workflow = optionalWorkflow.get();
            Checkpoint checkpoint = new Checkpoint();
            checkpoint.setWorkflow(workflow);
            checkpoint.setEdges(workflow.getEdges());
            checkpoint.setNodes(workflow.getNodes());
            checkpoint.setCreatedAt(new Date());
            return checkpointRepository.save(checkpoint);
        } else {
            throw new IllegalArgumentException("Workflow with the given Id does not exist.");
        }
    }
}
