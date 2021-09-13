package com.gw.tools;

import java.io.File;
import java.io.IOException;

import javax.websocket.Session;

import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.server.CommandServlet;

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

    
	public void saveHistory(String processid, String script, String history_id, String indicator){

		History history = histool.getHistoryById(history_id);

		if(bt.isNull(history)){

			history = new History();

		}

		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		history.setHistory_begin_time(bt.getCurrentSQLDate());
		
		history.setHistory_input(script);

        history.setHistory_id(history_id);

        history.setIndicator(indicator);

		histool.saveHistory(history);

        logger.debug("Saving history into database - " + history_id);

	}

    public void sendMessageWebSocket(String history_id, String httpsessionid, String message){
        
        try {
            
            Session wsout = CommandServlet.findSessionById(httpsessionid);

            if(!bt.isNull(wsout) && wsout.isOpen()){
                wsout.getBasicRemote().sendText(message);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public String executeCommonTasks(String history_id, String pid, String host, String pswd, String httpsessionid, boolean isjoin){

        String resp = null;

        History his = null;

        try{
            
            // sendMessageWebSocket(history_id, httpsessionid, "====== Start to process " + history_id);
 
            String code = pt.getCodeById(pid);

            this.saveHistory(pid, code, history_id, ExecutionStatus.RUNNING);

            

            his = histool.getHistoryById(history_id);

            JSONObject obj = (JSONObject)new JSONParser().parse(code);
                
            String operation = (String)obj.get("operation");
            
            JSONArray params = (JSONArray)obj.get("params");

            String filename = null;
            
            if(operation.equals("ShowResultMap") || operation.equals("DownloadData") ) {
                
                String filepath = (String)((JSONObject)params.get(0)).get("value");
                
                logger.debug("get result file path : " + filepath);
                
                filename = new File(filepath).getName();
                
    //				String dest = BaseTool.getGeoweaverRootPath() + SysDir.upload_file_path + "/" + filename;
                
                File folder = new File(bt.getFileTransferFolder());
                
                if(!folder.exists()) {
                    folder.mkdir();
                }
                
                String fileloc = bt.getFileTransferFolder() + "/" + filename;
                
                if(ht.islocal(host)) {
                    
                    resp = ft.download_local(filepath, fileloc);

                    sendMessageWebSocket(history_id, httpsessionid, resp);

                }else {

                    ft.scp_download(host, pswd, filepath, fileloc);

                    sendMessageWebSocket(history_id, httpsessionid, "File " + fileloc + " is downloaded from remote host.");
                    
                }
                
                
                logger.debug("result info: " + fileloc);
                
                // String ret = "{\"builtin\": true, \"history_id\": \"" + history_id + 
                //         "\", \"operation\":\""+operation+"\", \"filename\": \"" + filename + "\"}";
                
                // this.history_output = filename;
                
            }

            // String historyid = t.getHistory_id();

            resp = bt.isNull(resp)? "{\"history_id\": \""+history_id+
                    
                    "\", \"token\": \""+httpsessionid+

                    "\", \"operation\":\""+operation+ 
                    
                    "\", \"filename\": \"" + filename + 
                    
                    "\", \"ret\": \"success\"}": resp; 


            his = histool.getHistoryById(history_id);

            if(!bt.isNull(his)){

                if(resp.indexOf("failure")==-1) 
                
                    his.setIndicator(ExecutionStatus.DONE);
                
                else
                    
                    his.setIndicator(ExecutionStatus.FAILED);

            }

            his.setIndicator(ExecutionStatus.DONE);
            his.setHistory_end_time(bt.getCurrentSQLDate());
            his.setHistory_output(resp);
            histool.saveHistory(his);

        }catch(Exception e){
            
            e.printStackTrace();

            resp = "{\"history_id\": \""+history_id+
                    
                    "\", \"token\": \""+httpsessionid+
                    
                    "\", \"ret\": \"failed\"}";

            his.setIndicator(ExecutionStatus.FAILED);
                    
            sendMessageWebSocket(history_id, httpsessionid, e.getLocalizedMessage());

        }

        if(!bt.isNull(his)){
        
            his.setHistory_output(resp);

            his.setHistory_end_time(bt.getCurrentSQLDate());

            histool.saveHistory(his);
        
        }

        sendMessageWebSocket(history_id, httpsessionid, "====== Process " + history_id + " ended");

        return resp;

    }

}
