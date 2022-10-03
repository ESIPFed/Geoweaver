package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "process", description = "import a process from zip file")
public class ImportProcessCommand implements Runnable{

    @Parameters(index = "0", description = "Geoweaver workflow zip file path")
    String process_file_path;

    @Override
    public void run() {
        
        System.err.println("Not implemented yet. Please go to Geoweaver github issues to check out latest discussion.");
    }

}
