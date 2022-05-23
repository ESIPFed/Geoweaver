package com.gw;

import static org.assertj.core.api.Assertions.assertThat;

import com.gw.commands.TopEntryCommand;
import com.gw.utils.BeanTool;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.test.context.junit4.SpringRunner;

import picocli.CommandLine;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GeoweaverCLI.class})
// @ExtendWith(OutputCaptureExtension.class)
public class CLITests {

    @Rule
    public OutputCaptureRule output = new OutputCaptureRule();

    @Test
    public void testTopLevelHelp () {

        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute("help");
        assertThat(output).contains("resetpassword", "run", "list", "detail", "help");

    }

    @Test
    public void testListHelp(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"list", "--help"});
        assertThat(output).contains("--host       list hosts");
        assertThat(output).contains("--process    list processes");
        assertThat(output).contains("--workflow   list workflows");

    }

    @Test
    public void testListHost(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"list", "--host"});
        assertThat(output).contains("Host Id");
        assertThat(output).contains("Hostname");
        assertThat(output).contains("IP");
        assertThat(output).contains("Port");

    }

    @Test
    public void testListProcess(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"list", "--process"});
        assertThat(output).contains("Process Id");
        assertThat(output).contains("Name");
        assertThat(output).contains("Language");
        assertThat(output).contains("Description");

    }

    @Test
    public void testListWorkflow(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"list", "--workflow"});
        assertThat(output).contains("Workflow Id");
        assertThat(output).contains("Name");

    }

    @Test
    public void testListWithoutArgument(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"list"});
        assertThat(output).contains("Error: Missing required argument(s): ");

    }


    // TODO: Performing only negative tests on detail command for now. Need to add success tests for detail command.

    @Test
    public void testDetailHelp(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"detail", "--help"});
        assertThat(output).contains("--host-id=<hostId>");
        assertThat(output).contains("--process-id=<processId>");
        assertThat(output).contains("--workflow-id=<workflowId>");

    }

    @Test
    public void testDetailHostWithoutHostId(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"detail", "--host-id"});
        assertThat(output).contains("Missing required parameter for option '--host-id' (<hostId>)");

    }

    @Test
    public void testDetailHostWithWrongHostId(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"detail", "--host-id=wrongHostId"});
        assertThat(output).contains("No host found with id: wrongHostId");

    }

    @Test
    public void testDetailProcessWithoutProcessId(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"detail", "--process-id"});
        assertThat(output).contains("Missing required parameter for option '--process-id' (<processId>)");

    }

    @Test
    public void testDetailProcessWithWrongProcessId(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"detail", "--process-id=wrongProcessId"});
        assertThat(output).contains("No process found with id: wrongProcessId");

    }

    @Test
    public void testDetailWorkflowWithoutWorkflowId(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"detail", "--workflow-id"});
        assertThat(output).contains("Missing required parameter for option '--workflow-id' (<workflowId>)");

    }

    @Test
    public void testDetailWorkflowWithWrongWorkflowId(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"detail", "--workflow-id=wrongWorkflowId"});
        assertThat(output).contains("No workflow found with id: wrongWorkflowId");

    }

}
