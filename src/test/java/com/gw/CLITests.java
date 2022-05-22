package com.gw;

import static org.assertj.core.api.Assertions.assertThat;

import com.gw.commands.TopEntryCommand;
import com.gw.utils.BeanTool;

import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
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

}
