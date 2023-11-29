package com.gw.tools;

import com.gw.database.CheckpointRepository;
import com.gw.jpa.Checkpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Scope("prototype")
public class CheckpointTool {

    @Autowired
    private CheckpointRepository checkpointRepository;

    public List<Checkpoint> getCheckpointByWorkflowId(String workflowId) {
        return checkpointRepository.findByWorkflowId(workflowId);
    }
}
