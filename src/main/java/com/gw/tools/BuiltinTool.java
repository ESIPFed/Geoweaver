package com.gw.tools;

import java.io.File;

import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuiltinTool {
    
    @Autowired
    ProcessTool pt;

    
	@Autowired
	BaseTool bt;
	
	@Autowired
	FileTool ft;

    @Autowired
	HostTool ht;

    Logger logger = Logger.getLogger(this.getClass());

    public String executeCommonTasks(String pid, String host, String pswd, String httpsessionid, String history_id, boolean isjoin){

        String resp = null;

        try{
            
            String code = pt.getCodeById(pid);

            JSONObject obj = (JSONObject)new JSONParser().parse(code);
                
            String operation = (String)obj.get("operation");
            
            JSONArray params = (JSONArray)obj.get("params");
            
            if(operation.equals("ShowResultMap") || operation.equals("DownloadData") ) {
                
                String filepath = (String)((JSONObject)params.get(0)).get("value");
                
                logger.debug("get result file path : " + filepath);
                
    //				String filename = new RandomString(8).nextString();
                
                String filename = new File(filepath).getName();
                
    //				String dest = BaseTool.getGeoweaverRootPath() + SysDir.upload_file_path + "/" + filename;
                
                File folder = new File(bt.getFileTransferFolder());
                
                if(!folder.exists()) {
                    folder.mkdir();
                }
                
                String fileloc = bt.getFileTransferFolder() + "/" + filename;
                
                if(ht.islocal(host)) {
                    
                    ft.download_local(filepath, fileloc);
                    
                }else {

                    ft.scp_download(host, pswd, filepath, fileloc);
                    
                }
                
                
                logger.debug("result info: " + fileloc);
                
                String ret = "{\"builtin\": true, \"history_id\": \"" + history_id + 
                        "\", \"operation\":\""+operation+"\", \"filename\": \"" + filename + "\"}";
                
                // this.history_output = filename;
                
            }

            // String historyid = t.getHistory_id();
                
            resp = "{\"history_id\": \""+history_id+
                    
                    "\", \"token\": \""+httpsessionid+
                    
                    "\", \"ret\": \"success\"}"; 

        }catch(Exception e){
            e.printStackTrace();

        }
        

        return resp;

    }

}
