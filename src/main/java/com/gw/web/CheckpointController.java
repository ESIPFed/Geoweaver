package com.gw.web;

import com.gw.dto.checkpoint.CheckpointCreateRequest;
import com.gw.dto.checkpoint.CheckpointDTO;
import com.gw.jpa.Checkpoint;
import com.gw.tools.CheckpointTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkpoint")
public class CheckpointController {

    @Autowired
    private CheckpointTool checkpointTool;

    @GetMapping("/{workflowId}")
    public ResponseEntity<List<CheckpointDTO>> getAllCheckpoints(@PathVariable String workflowId) {
        List<Checkpoint> checkpoints = checkpointTool.getCheckpointByWorkflowId(workflowId);
        List<CheckpointDTO> checkpointDTOs = checkpoints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(checkpointDTOs);
    }

    private CheckpointDTO convertToDTO(Checkpoint checkpoint) {
        CheckpointDTO dto = new CheckpointDTO();
        dto.setId(checkpoint.getId());
        dto.setEdges(checkpoint.getEdges());
        dto.setNodes(checkpoint.getNodes());
        dto.setCreatedAt(checkpoint.getCreatedAt());
        return dto;
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

//    @GetMapping
}
