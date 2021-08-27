package com.gw.tools;

import java.io.File;

import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gw.jpa.History;

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

    @Autowired
    HistoryTool histool;

    Logger logger = Logger.getLogger(this.getClass());

    
	public void saveHistory(String processid, String script, String history_id){

		History history = histool.getHistoryById(history_id);

		if(bt.isNull(history)){

			history = new History();

		}

		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		history.setHistory_begin_time(bt.getCurrentSQLDate());
		
		history.setHistory_input(script);

        history.setHistory_id(history_id);

		histool.saveHistory(history);

	}

    public String executeCommonTasks(String history_id, String pid, String host, String pswd, String httpsessionid, boolean isjoin){

        String resp = null;

        try{
            
            String code = pt.getCodeById(pid);

            this.saveHistory(pid, code, history_id);

            JSONObject obj = (JSONObject)new JSONParser().parse(code);
                
            String operation = (String)obj.get("operation");
            
            JSONArray params = (JSONArray)obj.get("params");
            
            if(operation.equals("ShowResultMap") || operation.equals("DownloadData") ) {
                
                String filepath = (String)((JSONObject)params.get(0)).get("value");
                
                logger.debug("get result file path : " + filepath);
                
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
