package com.gw.commands;

import org.springframework.stereotype.Component;

import com.gw.jpa.History;
import com.gw.tools.HistoryTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.CommandLineUtil;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "history", description = "Show a history")
public class HistoryCommand   implements Runnable {

    @Parameters(index = "0", description = "history id")
    String history_id;

    @Override
    public void run() {

        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();

        HistoryTool ht = BeanTool.getBean(HistoryTool.class);

        BaseTool bt = BeanTool.getBean(BaseTool.class);

        History hist = ht.getHistoryById(history_id);

        if(hist != null) {

            table.setHeaders(new String[] { "History Id", "Status", "Begin Time", "End Time", "Input", "Output", "Notes" });
            table.addRow(new String[] {hist.getHistory_id(), hist.getIndicator().toString(), 
                bt.formatDate(hist.getHistory_begin_time()), bt.formatDate(hist.getHistory_end_time()), 
                hist.getHistory_input(), hist.getHistory_output(), hist.getHistory_notes()});

            table.print();

        }else {

            System.out.println(String.format("No history found with id: %s", history_id));
            
        }
        
    }
    
}
