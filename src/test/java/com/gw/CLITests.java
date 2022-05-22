package com.gw;

import com.gw.commands.TopEntryCommand;
import com.gw.utils.BeanTool;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import picocli.CommandLine;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {GeoweaverCLI.class})
public class CLITests {


    @Test
    public void testListHost(){
        
        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);
        new CommandLine(topEntryCommand).execute(new String[]{"list", "--host"});
        //check if the function is called or the result table columns are there

        // System.out.println("captured: "+output.getAll());
        // clr.run("list --host");
        //pass the context otherwise it will be lost after new CommandLine

    }
    
}
