package com.gw;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;

public class TempTest {

    Logger logger = Logger.getLogger(this.getClass());

    String testResourceFiles(){

		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();

		logger.debug(absolutePath);
		return absolutePath;
	}

    public static void main(String[] args){
        TempTest tester = new TempTest();
        BaseTool bt = new BaseTool();
        String testjson = bt.readStringFromFile(tester.testResourceFiles()+ "/conda.txt" );
        Scanner scanner = new Scanner(testjson);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] vals = line.split("\\s+");
            System.out.println(line + " - " + vals[0] + " - " + vals.length);
        }
    }
    
}
