package com.gw.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *Class SysDir.java
 *@author ziheng
 */

@Configuration
@ConfigurationProperties(prefix = "geoweaver")
public class SysDir {
	
	int worknumber = 1;
	
	String instantiationservletaddress0 = null; //WorkflowCore instantiation servlet 0 - bind LogicProcess to MessageType
	
	String instantiationservletaddress = null; //WorkflowCore instantiation servlet 1 - Transform LogicProcess and MessageType to BPEL Workflow
	
	String executionservletaddress = null;
	
	String registrationaddress = null;
	
	String NOTIFICATION_EMAIL  = null;
    
	String NOTIFICATION_EMAIL_SERVICE_URL = null;
    
	String CACHE_OPERATION = null;
    
	String CACHE_SERVICE_URL = null;
    
	String CACHE_DATA_URLPREFIX = null;
    
	String CSISS_CSW_URL = null;
    
	String prefixurl = null;
    
	String ncWMSURL = null; 
    
	String ncUsername = null; 
    
	String ncPassword = null;
    
	String covali_file_path = null;
	
	String geoweaver_file_path = null;
    
	String upload_file_path = null;
	
	String allowed_ssh_hosts = null;
	
	String allowed_ssh_clients = null;
	
	String secret_properties_path = null;
    
	String temp_file_path = null;

	String thredds_harvester_path = null;

	String database_driver = null;

	String database_url = null;
	
	String database_docker_url = null;

	String database_user = null;

	String database_password = null;
	
	String workspace = null;

}