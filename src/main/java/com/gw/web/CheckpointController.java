package com.gw.web;

import com.gw.dto.checkpoint.CheckpointCreateRequest;
import com.gw.dto.checkpoint.CheckpointDTO;
import com.gw.dto.checkpoint.CheckpointRestoreDTO;
import com.gw.jpa.Checkpoint;
import com.gw.tools.CheckpointTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkpoint")
public class CheckpointController {

    @Autowired
    private CheckpointTool checkpointTool;

    @GetMapping("/{workflowId}")
    public ResponseEntity<List<CheckpointDTO>> getAllCheckpoints(@PathVariable String workflowId) {
        List<Checkpoint> checkpoints = checkpointTool.getCheckpointByWorkflowId(workflowId);
        List<CheckpointDTO> checkpointDTOs = checkpoints.stream()
                .map(checkpointTool::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(checkpointDTOs);
    }


    @PostMapping("/create")
    public ResponseEntity<Checkpoint> createCheckpoint(@RequestBody CheckpointCreateRequest createRequest) {
        try {
            Checkpoint createdCheckpoint = checkpointTool.createCheckpoint(createRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCheckpoint);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/restoreWorkflow")
    public ResponseEntity<Checkpoint> restoreWorkflow(@RequestBody CheckpointRestoreDTO restoreDTO) {
        try {
            Checkpoint checkpointRestore = checkpointTool.restoreCheckpoint(restoreDTO.getWorkflowId(),
                    restoreDTO.getExecutionId());
            return ResponseEntity.status(HttpStatus.OK).body(checkpointRestore);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
