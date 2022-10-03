package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component
@Command(name = "process")
public class ExportProcessCommand implements Runnable {

    @Override
    public void run() {
        
        System.err.println("Not implemented yet. Please go to Geoweaver github issues to check out latest discussion.");
        
    }

}
