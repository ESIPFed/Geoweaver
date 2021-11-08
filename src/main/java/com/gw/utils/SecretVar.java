package com.gw.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Properties;

public class SecretVar {

    static String credentials;

    static String clientid;

    static String clientsecret;

    static String refreshtoken;
    
	static Properties readProperties(String path) {
		Properties p = new Properties();

		try {
			FileInputStream fileIn = new FileInputStream(path);

			p.load(fileIn);
			fileIn.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return p;
	}
	
	

	static{
		
		// initialize from config file
		try {
			
			BaseTool t = new BaseTool();

			String configFile = t.getClassPath()+FileSystems.getDefault().getSeparator()+"secret.properties";
			
			Properties p = readProperties(configFile);

			credentials = p.getProperty("gmail.credentials");

            clientid = p.getProperty("gmail.clientid");

            clientsecret = p.getProperty("gmail.clientsecret");

            refreshtoken = p.getProperty("gmail.refreshtoken");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
}
