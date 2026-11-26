package com.gw.commands;

import com.gw.utils.RunLogStore;
import com.gw.jpa.History;
import com.gw.tools.ExecutionTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.CommandLineUtil;
import com.gw.utils.InternalRunCredential;
import com.gw.utils.RandomString;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "process", description = "Run a process and wait for it to end (or --detach)")
public class RunProcessCommand implements Runnable {

  @Parameters(index = "0", description = "process id to run")
  String processid;

  @Option(
      names = {"-h", "--host"},
      description = "host to run on",
      defaultValue = "100001")
  String hostid;

  @Option(
      names = {"-e", "--environment"},
      description = "environment to run on",
      defaultValue = "default_option")
  String envid;

  @Option(
      names = {"-p", "--password"},
      description = "password to the target host")
  String pass;

  @Option(
      names = "--detach",
      description = "start the run in the background, print its run id and return; it keeps running after you log out")
  boolean detach;

  @Option(names = "--run-id", hidden = true)
  String runId;

  public void run() {

    if (detach) {
      CliDetach.launch(BaseTool.isNull(pass) ? null : new String[] {pass});
      return;
    }
    if (BaseTool.isNull(pass)) {
      String[] inherited = CliDetach.inheritedPasswords();
      if (inherited != null && inherited.length > 0) pass = inherited[0];
    }

    ProcessTool pt = BeanTool.getBean(ProcessTool.class);

    // Stop pressed in a browser connected to the same database reaches this run.
    com.gw.web.GeoweaverController.watchStopRequests();

    BaseTool bt = BeanTool.getBean(BaseTool.class);

    HistoryTool histool = BeanTool.getBean(HistoryTool.class);

    String historyid =
        runId != null && CliDetach.RUN_ID.matcher(runId).matches()
            ? runId
            : new RandomString(18).nextString();
    com.gw.api.v1.AgentApiAuthContext.setTokenFingerprint(CliAgent.ACTOR);

    ExecutionTool et = BeanTool.getBean(ExecutionTool.class);

    CliOutput.line(String.format("Starting process %s", processid));
    System.err.println("Run id: " + historyid);

    // The CLI user already has a shell on this machine, so localhost needs no GUI password here.
    if (BaseTool.isNull(pass) && bt.islocal(hostid)) {
      pass = InternalRunCredential.get();
    }

    String response =
        et.executeProcess(historyid, processid, hostid, pass, "runfromcmd", true, envid);

    History hist = null;

    try {

      while (true) {

        TimeUnit.SECONDS.sleep(2);

        hist = histool.getHistoryById(historyid);

        if (histool.checkIfEnd(hist)) break;
      }

    } catch (InterruptedException e) {

      e.printStackTrace();
    }

    if (CliOutput.json()) {
      CliOutput.print(BeanTool.getBean(com.gw.api.v1.service.AgentRunSupport.class).getRun(historyid, 4000));
      if (!"Done".equals(String.valueOf(hist.getIndicator()))) CliOutput.setExitCode(1);
      return;
    }

    System.out.println("Execution finished");

    System.out.println(
        String.format(
            "Total time cost: %d seconds",
            BaseTool.calculateDuration(hist.getHistory_begin_time(), hist.getHistory_end_time())));

    CommandLineUtil.CommandLineTable table = new CommandLineUtil.CommandLineTable();

    table.setHeaders(
        new String[] {
          "History Id", "Status", "Begin Time", "End Time", "Input", "Output", "Notes"
        });

    table.addRow(
        new String[] {
          hist.getHistory_id(),
          hist.getIndicator().toString(),
          bt.formatDate(hist.getHistory_begin_time()),
          bt.formatDate(hist.getHistory_end_time()),
          hist.getHistory_input(),
          RunLogStore.readFull(hist.getHistory_id(), hist.getHistory_output()),
          hist.getHistory_notes()
        });

    table.print();
  }
}
