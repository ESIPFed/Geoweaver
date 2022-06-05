package com.gw.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecretVar {

    String credentials;

    String clientid;

    String clientsecret;

    String refreshtoken;

    

    @Autowired
    BaseTool bt;

    void refresh(){
		
		// initialize from config file
		try {

            // if(BaseTool.isNull(credentials)){

                String configFile = bt.getGWWorkspaceFolder() + "secret.properties";

                if(new File(configFile).exists()){

                    Properties p = readProperties(configFile);

                    credentials = p.getProperty("gmail.credentials");

                    clientid = p.getProperty("gmail.clientid");

                    clientsecret = p.getProperty("gmail.clientsecret");

                    refreshtoken = p.getProperty("gmail.refreshtoken");
                    
                }else{
                    //the file doesn't exist, create a new one
                    bt.writeString2File("gmail.credentials=geoweaver\n"+
                    "gmail.clientid=geoweaver\n"+
                    "gmail.clientsecret=geoweaver\n"+
                    "gmail.refreshtoken=geoweaver", configFile);

                }

            // }
			
            

		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
        
    public String getCredentials() {
        return this.credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getClientid() {
        return this.clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getClientsecret() {
        return this.clientsecret;
    }

    public void setClientsecret(String clientsecret) {
        this.clientsecret = clientsecret;
    }

    public String getRefreshtoken() {
        return this.refreshtoken;
    }

    public void setRefreshtoken(String refreshtoken) {
        this.refreshtoken = refreshtoken;
    }


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
	
	

	
}
