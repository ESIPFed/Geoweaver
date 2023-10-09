package com.gw.commands;

import org.springframework.stereotype.Component;

import com.gw.jpa.History;
import com.gw.tools.HistoryTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.CommandLineUtil;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;


/**
 * This class represents a command-line command for displaying the history of a process or workflow run.
 * It uses the picocli library to define the command and its parameters.
 * When executed, it retrieves the history details and displays them in a table format.
 */
@Component
@Command(name = "history", description = "Show a history of a process/workflow run")
public class HistoryCommand implements Runnable {

    @Parameters(index = "0", description = "History ID")
    String history_id;

    /**
     * The run method is called when this command is executed.
     * It retrieves the history details for the specified history_id and displays them in a table.
     */
    @Override
    public void run() {
        // Create a table to display the history details.
        CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();

        // Get an instance of the HistoryTool for working with history data.
        HistoryTool ht = BeanTool.getBean(HistoryTool.class);

        // Get an instance of the BaseTool for utility functions.
        BaseTool bt = BeanTool.getBean(BaseTool.class);

        // Retrieve the history details for the specified history_id.
        History hist = ht.getHistoryById(history_id);

        // Check if a valid history record was found.
        if (hist != null && hist.getHistory_process() != null) {
            // Set the table headers.
            table.setHeaders(new String[] { "History Id", "Status", "Begin Time", "End Time", "Input", "Output", "Notes" });

            // Add a row with history details to the table.
            table.addRow(new String[] {
                hist.getHistory_id(),
                hist.getIndicator().toString(),
                bt.formatDate(hist.getHistory_begin_time()),
                bt.formatDate(hist.getHistory_end_time()),
                hist.getHistory_input(),
                hist.getHistory_output(),
                hist.getHistory_notes()
            });

            // Print the table to the console.
            table.print();
        } else {
            // If no history record was found, display an error message.
            System.out.println(String.format("No history found with ID: %s", history_id));
        }
    }
}
