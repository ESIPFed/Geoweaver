package com.gw.commands;

import java.util.Collection;
import java.util.List;

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

/**
 * This class represents a command-line command for listing resources in Geoweaver.
 * Users can list hosts, processes, or workflows using different command options.
 * The `picocli` library is used for defining the command and its parameters.
 */
class requiredListCommandOptions {

    @Option(names = { "--host" }, description = "List hosts")
    Boolean host;

    @Option(names = { "--process" }, description = "List processes")
    Boolean process;

    @Option(names = { "--workflow" }, description = "List workflows")
    Boolean workflow;

    @Option(names = { "--help" }, usageHelp = true, description = "Displays this help message")
    Boolean help;
}

@Component
@Command(name = "list", description = "List the resources in Geoweaver")
public class ListCommand implements Runnable {

    @ArgGroup(exclusive = false, multiplicity = "1")
    requiredListCommandOptions requiredCommandOptions;

    /**
     * The run method is called when this command is executed.
     * Depending on the specified resource type (host, process, or workflow), it retrieves
     * and displays the corresponding resources in a table format.
     */
    public void run() {
        // Create a table to display the resource details.
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        if (requiredCommandOptions.host != null && requiredCommandOptions.host) {
            // If listing hosts is requested.
            HostTool ht = BeanTool.getBean(HostTool.class);
            List<Host> allhosts = ht.getAllHosts();

            // Set headers for the table.
            table.setHeaders(new String[] { "Host Id", "Hostname", "IP", "Port", "Username" });

            // Add rows with host details to the table.
            for (Host h : allhosts) {
                table.addRow(h.getId(), h.getName(), h.getIp(), h.getPort(), h.getUsername());
            }

        } else if (requiredCommandOptions.process != null && requiredCommandOptions.process) {
            // If listing processes is requested.
            ProcessRepository pr = BeanTool.getBean(ProcessRepository.class);
            Collection<GWProcess> allPublicProcesses = pr.findAllPublic();

            // Set headers for the table.
            table.setHeaders(new String[] { "Process Id", "Name", "Language", "Description" });

            // Add rows with process details to the table.
            for (GWProcess p : allPublicProcesses) {
                table.addRow(p.getId(), p.getName(), p.getLang(), p.getDescription());
            }

        } else if (requiredCommandOptions.workflow != null && requiredCommandOptions.workflow) {
            // If listing workflows is requested.
            WorkflowRepository workflowrepository = BeanTool.getBean(WorkflowRepository.class);
            Collection<Workflow> workflows = (Collection<Workflow>) workflowrepository.findAll();

            // Set headers for the table.
            table.setHeaders(new String[] { "Workflow Id", "Name" });

            // Add rows with workflow details to the table.
            for (Workflow workflow : workflows) {
                table.addRow(workflow.getId().toString(), workflow.getName().toString());
            }
        }

        // Print the table to the console.
        table.print();
    }
}
