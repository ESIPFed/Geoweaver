package com.gw.tools;

import com.gw.database.CheckpointRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.dto.checkpoint.CheckpointCreateRequest;
import com.gw.jpa.Checkpoint;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class CheckpointTool {

    @Autowired
    private CheckpointRepository checkpointRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ProcessRepository processRepository;

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

    public Checkpoint restoreCheckpoint(String workflowId, String executionId) {
        List<Checkpoint> optionalCheckpoint = checkpointRepository.findCheckpointByExecutionId(executionId);
        Checkpoint checkpoint = optionalCheckpoint.get(0);
        System.out.println(checkpoint);
        Optional<Workflow> optionalWorkflow = workflowRepository.findById(workflowId);
        if (optionalWorkflow.isPresent()) {
            Workflow workflow = optionalWorkflow.get();
            workflow.setNodes(checkpoint.getNodes());
            workflow.setEdges(checkpoint.getEdges());
            workflowRepository.save(workflow);
        } else {
            throw new IllegalArgumentException("Workflow with the given workflowId does not exist.");
        }

        // restore code in each process
        List<History> workflowHistory = historyRepository.findHistoryWithExecutionId(executionId, workflowId);
        History history = workflowHistory.get(0);
        String[] historyInput = history.getHistory_input().split(";");
        List<String> historyProcessIdsList = new ArrayList<>();
        for (String s : historyInput) {
            historyProcessIdsList.add(s.split("-")[0]);
        }
        System.out.println("History Process IDs: " + historyProcessIdsList);

        // get history ID for these processes
        String historyOutput = history.getHistory_output();
        List<String> historyIdList = new ArrayList<>(Arrays.asList(historyOutput.split(";")));
        System.out.println("History IDs:" + historyIdList);

        // fetch code from history and set it to process in GWProcess
        for (int i = 0; i < historyProcessIdsList.size(); i++) {
            List<History> processHistory = historyRepository.findByProcessId(historyProcessIdsList.get(i));
            String code = processHistory.get(i).getHistory_input();

            Optional<GWProcess> p = processRepository.findById(historyProcessIdsList.get(i));

            if (p.isPresent()) {
                GWProcess process = p.get();
                process.setCode(code);
                processRepository.save(process);
            } else {
                System.out.println("Failed to restore" + p);
            }
        }

        return checkpoint;

    }
}
