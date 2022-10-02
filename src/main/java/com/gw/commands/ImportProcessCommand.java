package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component
@Command(name = "process", description = "import a process from zip file")
public class ImportProcessCommand implements Runnable{

    @Override
    public void run() {
        
        
    }

}
