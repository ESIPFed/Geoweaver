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

class requiredListCommandOptions {

    @Option(names = { "--host" }, description = "list hosts")
    Boolean host;

    @Option(names = { "--process" }, description = "list processes")
    Boolean process;

    @Option(names = { "--workflow" }, description = "list workflows")
    Boolean workflow;

    @Option(names = { "--help" }, usageHelp = true, description = "displays this help message")
    Boolean help;

}

@Component
@Command(name = "list", description = "list the resources in Geoweaver")
public class ListCommand implements Runnable {

    @ArgGroup(exclusive = false, multiplicity = "1")
    requiredListCommandOptions requiredCommandOptions;
    

    public void run() {
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();
        if(requiredCommandOptions.host != null && requiredCommandOptions.host) {
            HostTool ht = BeanTool.getBean(HostTool.class);
            List<Host> allhosts = ht.getAllHosts();

            table.setHeaders(new String[] { "Host Id", "Hostname", "IP", "Port", "Username" });

            for(Host h : allhosts) {
                table.addRow(h.getId(), h.getName(), h.getIp(), h.getPort(), h.getUsername());
            }

        }else if(requiredCommandOptions.process != null && requiredCommandOptions.process) {
            ProcessRepository pr = BeanTool.getBean(ProcessRepository.class);

            Collection<GWProcess> allPublicProcesses = pr.findAllPublic();

            // Set Headers
            table.setHeaders(new String[] { "Process Id", "Name", "Language", "Description" });

            // Add Rows
            for(GWProcess p : allPublicProcesses) {
                table.addRow(p.getId(), p.getName(), p.getLang(), p.getDescription());
            }

        }else if(requiredCommandOptions.workflow != null && requiredCommandOptions.workflow) {
            
            WorkflowRepository workflowrepository = BeanTool.getBean(WorkflowRepository.class);
            
            Collection<Workflow> workflows = (Collection<Workflow>) workflowrepository.findAll();
            
            // Set headers
            table.setHeaders(new String[] { "Workflow Id", "Name" });

            // Keep adding rows to the table
            for(Workflow workflow:workflows) {
                table.addRow(workflow.getId().toString(), workflow.getName().toString());
            }

        }

        // Print table
        table.print();
    }
    
}
