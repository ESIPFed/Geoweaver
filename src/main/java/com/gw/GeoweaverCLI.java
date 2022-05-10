package com.gw;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.stereotype.Service;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

public class GeoweaverCLI implements CommandLineRunner {

    final TopEntryCommand topEntryCommand;
    final IFactory factory;

    @Override
    public void run(String... args) {

        // System.exit(new CommandLine(topEntryCommand, factory).execute(args));
        new CommandLine(topEntryCommand, factory).execute(args);

    }

    public GeoweaverCLI(TopEntryCommand topEntryCommand, IFactory factory) {
        this.topEntryCommand = topEntryCommand;
        this.factory = factory;
    }
    
}
