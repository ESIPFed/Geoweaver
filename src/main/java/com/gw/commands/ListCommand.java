package com.gw.commands;

import java.io.Console;
import java.util.Collection;

import com.gw.database.HostRepository;
import com.gw.jpa.Host;

import org.springframework.beans.factory.annotation.Autowired;
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
@Command(name = "list", description = "list details")
public class ListCommand implements Runnable {

    @Autowired
    HostRepository hostRepository;

    @ArgGroup(exclusive = false, multiplicity = "1")
    requiredCommandOptions requiredCommandOptions;
    

    public void run() {
        Console console = System.console();
        if(requiredCommandOptions.host) {
            console.printf("Listing hosts (%d)%n", hostRepository.count());
            console.printf("format: id - name - ip - port - username%n");
            Collection<Host> hosts = (Collection<Host>) hostRepository.findAll();
            for(Host host : hosts) {
                console.printf("%s - %s - %s - %s - %s%n", host.getId(), host.getName(), host.getIp(), host.getPort(), host.getUsername());
            }
        }
    }
    
}
