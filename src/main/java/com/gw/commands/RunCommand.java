package com.gw.commands;

import picocli.CommandLine.Command;

@Command(name = "run")
public class RunCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("running run command");
    }
    
}
