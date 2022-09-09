package com.gw.commands;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.gw.jpa.History;
import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.CommandLineUtil;
import com.gw.utils.RandomString;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "process")
public class RunProcessCommand  implements Runnable {

    @Parameters(index = "0", description = "process id to run")
    String processid;

    @Option(names = { "-h", "--host" }, description = "host to run on", defaultValue = "10001")
    String hostid;

    @Option(names = { "-e", "--environment" }, description = "environment to run on", defaultValue = "default_option")
    String envid;

    @Option(names = { "-p", "--password" }, description = "password to the target host")
    String pass;

    public void run(){

        ProcessTool pt = BeanTool.getBean(ProcessTool.class);

        BaseTool bt = BeanTool.getBean(BaseTool.class);

        HistoryTool histool = BeanTool.getBean(HistoryTool.class);

        String historyid = new RandomString(18).nextString();

        ExecutionTool et = BeanTool.getBean(ExecutionTool.class);

        System.out.println(String.format("Staring process %s", processid));

        String response = et.executeProcess(historyid, processid, hostid, pass, "runfromcmd", true, envid);
        
        History hist = null;

        try {
        
            while(true){
        
                TimeUnit.SECONDS.sleep(2);
        
                hist = histool.getHistoryById(historyid);
        
                if(histool.checkIfEnd(hist)) break;
            
            }

        } catch (InterruptedException e) {
            
            e.printStackTrace();
        
        }

        System.out.println("Execution finished");

        System.out.println(String.format("Total time cost: %o seconds", 
                           BaseTool.calculateDuration(hist.getHistory_begin_time(), hist.getHistory_end_time())));
        
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();

        table.setHeaders(new String[] { "History Id", "Status", "Begin Time", "End Time", "Input", "Output", "Notes" });
        
        table.addRow(new String[] {hist.getHistory_id(), hist.getIndicator().toString(), 
            bt.formatDate(hist.getHistory_begin_time()), bt.formatDate(hist.getHistory_end_time()), 
            hist.getHistory_input(), hist.getHistory_output(), hist.getHistory_notes()});

        table.print();

    }
    
}
