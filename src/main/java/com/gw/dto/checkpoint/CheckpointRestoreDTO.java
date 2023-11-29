package com.gw.dto.checkpoint;

import java.util.UUID;

public class CheckpointRestoreDTO {

    private UUID uuid;

    private String workflowId;


    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
}
