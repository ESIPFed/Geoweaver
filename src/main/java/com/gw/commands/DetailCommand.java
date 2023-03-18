package com.gw.commands;

import java.util.Optional;

import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.GWProcess;
import com.gw.jpa.Host;
import com.gw.jpa.Workflow;
import com.gw.tools.HostTool;
import com.gw.utils.BeanTool;
import com.gw.utils.CommandLineUtil;

import org.springframework.stereotype.Component;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


class requiredDetailCommandOptions {

    @Option(names = { "--host-id" }, description = "Host Id to get details for")
    String hostId;

    @Option(names = { "--process-id" }, description = "Process Id to get details for")
    String processId;

    @Option(names = { "--workflow-id" }, description = "Workflow Id to get details for")
    String workflowId;

    @Option(names = { "--help" }, usageHelp = true, description = "displays this help message")
    Boolean help;

}

@Component
@Command(name = "detail", description = "Show the detail of a resource (workflow, process, host)")
public class DetailCommand implements Runnable {
    
    @ArgGroup(exclusive = false, multiplicity = "1")
    requiredDetailCommandOptions requiredCommandOptions;

    public void run() {

        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();

        if(requiredCommandOptions.hostId != null) {
            System.out.println("HostId: " + requiredCommandOptions.hostId);
            HostTool ht = BeanTool.getBean(HostTool.class);

            Host host = ht.getHostById(requiredCommandOptions.hostId);

            if(host != null) {

                table.setHeaders(new String[] { "Host Id", "Hostname", "IP", "Port", "Username", "Owner", "type", "url", "Confidential?" });
                table.addRow(host.getId(), host.getName(), host.getIp(), host.getPort(), host.getUsername(), host.getOwner(), host.getType(), host.getUrl(), host.getConfidential());

            }else {

                table.setHeaders(new String[] { "Input", "Error" });
                table.addRow("HostId", "No host found with id: " + requiredCommandOptions.hostId);

            }


        } else if(requiredCommandOptions.processId != null) {

            ProcessRepository pr = BeanTool.getBean(ProcessRepository.class);

            Optional<GWProcess> process = pr.findById(requiredCommandOptions.processId);

            if(process.isPresent()) {
                GWProcess p = process.get();
                // Set Headers for table
                table.setHeaders(new String[] { "Process Id", "Process Name", "Process Description", "Process Language", "Process Owner", "Process is Confidential?" });
                
                // Add row to table
                table.addRow(p.getId(), p.getName(), p.getDescription(), p.getLang(), p.getOwner(), p.getConfidential());

            } else {

                // No process found
                // Set error headers
                table.setHeaders(new String[] { "Input", "Error" });

                // Add error row
                table.addRow("Process Id", "No process found with id: " + requiredCommandOptions.processId);

            }

        } else if(requiredCommandOptions.workflowId != null) {

            WorkflowRepository workflowrepository = BeanTool.getBean(WorkflowRepository.class);

            Optional<Workflow> workflow = workflowrepository.findById(requiredCommandOptions.workflowId);

            if(workflow.isPresent()) {

                Workflow w = workflow.get();

                // Set Headers for table
                table.setHeaders(new String[] { "Workflow Id", "Name", "Description", "Owner", "Confidential?" });

                // Add row to table
                table.addRow(w.getId(), w.getName(), w.getDescription(), w.getOwner(), w.getConfidential());

            } else {

                // No workflow found
                // Set error headers
                table.setHeaders(new String[] { "Input", "Error" });

                // Add error row
                table.addRow("Workflow Id", "No workflow found with id: " + requiredCommandOptions.workflowId);

            }

            System.out.println("WorkflowId: " + requiredCommandOptions.workflowId);
        }

        // Print table
        table.print();
    }

}
