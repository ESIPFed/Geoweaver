package com.gw.commands;

import java.io.Console;
import java.util.Collection;
import java.util.List;

import com.gw.database.HostRepository;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.Host;
import com.gw.jpa.Workflow;
import com.gw.tools.HostTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

class requiredCommandOptions {
    @Option(names = { "--host" }, description = "list hosts")
    Boolean host;

    @Option(names = { "--process" }, description = "list processes")
    Boolean process;

    @Option(names = { "--workflow" }, description = "list workflows")
    Boolean workflow;
}

@Component
@Command(name = "list", description = "list the resources in Geoweaver")
public class ListCommand implements Runnable {

    @Bean
    HostTool ht(){
        return new HostTool();
    }
    // @Autowired
    // HostRepository hostRepository;

    @Autowired
    ProcessRepository processrepository;

    @Autowired
    WorkflowRepository workflowrepository;

    @ArgGroup(exclusive = false, multiplicity = "1")
    requiredCommandOptions requiredCommandOptions;
    

    public void run() {
        Console console = System.console();
        if(requiredCommandOptions.host != null && requiredCommandOptions.host) {
            List<Host> allhosts = ht().getAllHosts();
            console.printf("Listing hosts (%d)%n", allhosts.size());
            console.printf("format: id - name - ip - port - username%n");
            for(Host host : allhosts) {
                console.printf("%s - %s - %s - %s - %s%n", host.getId(), host.getName(), host.getIp(), host.getPort(), host.getUsername());
            }
        }else if(requiredCommandOptions.process != null && requiredCommandOptions.process) {
            console.printf("Listing processes (%d)%n", processrepository.count());
        }else if(requiredCommandOptions.workflow != null && requiredCommandOptions.workflow) {
            console.printf("Listing workflows (%d)%n", workflowrepository.count());
            console.printf("format: id - name%n");
            Collection<Workflow> workflows = (Collection<Workflow>) workflowrepository.findAll();
            for(Workflow workflow:workflows) {
                console.printf("%s - %s%n", workflow.getId(), workflow.getName());
            }
        }
    }
    
}
