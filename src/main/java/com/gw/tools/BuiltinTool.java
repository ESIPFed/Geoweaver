package com.gw.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Arrays;

import javax.websocket.Session;

import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.server.CommandServlet;

// import com.amazonaws.AmazonServiceException;
// import com.amazonaws.regions.Regions;
// import com.amazonaws.services.s3.AmazonS3;
// import com.amazonaws.services.s3.AmazonS3ClientBuilder;
// import com.amazonaws.services.s3.model.AccessControlList;
// import com.amazonaws.services.s3.model.Grant;
// import com.amazonaws.auth.AWSCredentials;
// import com.amazonaws.auth.BasicAWSCredentials;
// import com.amazonaws.auth.AWSStaticCredentialsProvider;
// import com.amazonaws.services.s3.model.ObjectListing;
// // import com.amazonaws.services.s3.model.S3ObjectSummary;
// import com.amazonaws.services.s3.model.ListObjectsV2Result;
// import com.amazonaws.services.s3.model.S3ObjectSummary;
// import com.amazonaws.services.s3.model.S3Object;
// import com.amazonaws.services.s3.model.S3ObjectInputStream;
// import com.amazonaws.services.s3.model.Bucket;


@Service
@Scope("prototype")
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

		if(BaseTool.isNull(history)){

			history = new History();

		}

		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		history.setHistory_begin_time(BaseTool.getCurrentSQLDate());
		
		history.setHistory_input(script);

        history.setHistory_id(history_id);

        history.setIndicator(indicator);

		histool.saveHistory(history);

        logger.debug("Saving history into database - " + history_id);

	}

    public void sendMessageWebSocket(String history_id, String httpsessionid, String message){
        
        try {
            
            Session wsout = CommandServlet.findSessionById(httpsessionid);

            if(!BaseTool.isNull(wsout) && wsout.isOpen()){
                wsout.getBasicRemote().sendText(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String executeCommonTasks(String history_id, String pid, String host, String pswd, String httpsessionid, boolean isjoin){

        String resp = null;

        History his = null;

        try{

            String code = pt.getCodeById(pid);

            this.saveHistory(pid, code, history_id, ExecutionStatus.RUNNING);

            his = histool.getHistoryById(history_id);

            JSONObject obj = (JSONObject)new JSONParser().parse(code);
                
            String operation = (String)obj.get("operation");
            JSONArray params = (JSONArray)obj.get("params");

            if (operation.equals("AWS S3")) {

                // String providedParams = (String)((JSONObject)params.get(0)).get("value");
                // String[] parsedParams = providedParams.split(" ");
                // System.out.println("{{Parsed Params}}: "+Arrays.toString(parsedParams));
                // // System.out.println(parsedParams[5]);

                // String accessKey = "";
                // String SecretKey = "";
                // String bucket = "";
                // String region = "";

                // for (int i = 0; i < parsedParams.length; i++) {
                //     // System.out.println("{{Looping Through}}: "+parsedParams[i]);
                //     if (parsedParams[i].toLowerCase().equals("-accesskey")){
                //         accessKey = parsedParams[i+1];

                //     } else if (parsedParams[i].toLowerCase().equals("-secretkey")) {
                //         SecretKey = parsedParams[i+1];

                //     } else if (parsedParams[i].toLowerCase().equals("-bucket")) {
                //         bucket = parsedParams[i+1];

                //     } else if (parsedParams[i].toLowerCase().equals("-region")) {
                //         region = parsedParams[i+1];
                //     }
                // }

                // System.out.println("{{Builtin Paramas AcessKey}}: "+accessKey);
                // System.out.println("{{Builtin Paramas SecretKey}}: "+SecretKey);
                // System.out.println("{{Builtin Paramas Bucket}}: "+bucket);
                // System.out.println("{{Builtin Paramas Region}}: "+region);
                
                // // Authenticate with AWS using `AccessKey` & `SecretKey`
                // AWSCredentials awsCreds = new BasicAWSCredentials(
                //     accessKey, 
                //     SecretKey
                // );

                // System.out.println("{{AWS Creds}}: "+awsCreds.toString());

                // // // Configure AWS Client using AWS Creds
                // AmazonS3 s3client = AmazonS3ClientBuilder
                //     .standard()
                //     .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                //     .withRegion(region)
                //     .build();

                // System.out.println("{{AWS Client}}: "+s3client.toString());

                // List<Bucket> buckets = s3client.listBuckets();
                // sendMessageWebSocket(history_id, httpsessionid, "############### Buckets ###########");
                // sendMessageWebSocket(history_id, httpsessionid, "Available Buckets in S3 Enviroment:");
                // for(Bucket Listbucket : buckets) {
                //     // System.out.println(Listbucket.getName());
                //     sendMessageWebSocket(history_id, httpsessionid, "- "+Listbucket.getName());
                // }
                // sendMessageWebSocket(history_id, httpsessionid, "########## End of Buckets ###########");


                // ListObjectsV2Result result = s3client.listObjectsV2(bucket);
                // System.out.println("{{AWS Bucket Objects}}: " + result.getObjectSummaries().toString());
                // // resp = result.getObjectSummaries().toString();
                
                // sendMessageWebSocket(history_id, httpsessionid, "\n############### Files ###########");
                // sendMessageWebSocket(history_id, httpsessionid, "Available Files in selected bucket:");
                // List<S3ObjectSummary> objects = result.getObjectSummaries();
                // for (S3ObjectSummary os : objects) {
                //     // System.out.println("{{AWS Bucket Objects}}: " + os.getKey());
                //     sendMessageWebSocket(history_id, httpsessionid, "- "+os.getKey());
                // }
                // sendMessageWebSocket(history_id, httpsessionid, "######### End of Files ###########");
                


                // S3Object s3object = s3client.getObject("geoweaver", "TestData.csv");
                // S3ObjectInputStream inputStream = s3object.getObjectContent();
                // resp = inputStream.toString();

                // sendMessageWebSocket(history_id, httpsessionid, resp);

                throw new UnsupportedOperationException("AWS S3 is not supported yet");

            }else {

                String filename = null;
                
                String filepath = (String)((JSONObject)params.get(0)).get("value");
                
                logger.debug("get result file path : " + filepath);
                
                filename = new File(filepath).getName();
                
                int extIndex = filename.lastIndexOf(".");

                String fileExtension = filename.substring(extIndex);

                File folder = new File(bt.getFileTransferFolder());
                
                if(!folder.exists()) {
                    folder.mkdir();
                }
                
                String fileloc = bt.getFileTransferFolder() + filename;
                
                if(bt.islocal(host)) {

                    resp = ft.download_local(filepath, fileloc);
                    
                    sendMessageWebSocket(history_id, httpsessionid, resp);

                }else {

                    ft.scp_download(host, pswd, filepath, fileloc);

                    sendMessageWebSocket(history_id, httpsessionid, "File " + fileloc + " is downloaded from remote host.");
                        
                }
                
                logger.debug("result info: " + fileloc);

                resp = BaseTool.isNull(resp)? "{\"history_id\": \""+history_id+
                        
                        "\", \"token\": \""+httpsessionid+

                        "\", \"operation\":\""+operation+ 
                        
                        "\", \"filename\": \"" + filename + 
                        
                        "\", \"ret\": \"success\"}": resp; 

            }

            resp = BaseTool.isNull(resp)? "{\"history_id\": \""+history_id+
                        
            "\", \"token\": \""+httpsessionid+

            "\", \"operation\":\""+operation+ 
            
            "\", \"ret\": \"success\"}": resp; 

            his = histool.getHistoryById(history_id);

            if(!BaseTool.isNull(his)){

                if(resp.indexOf("failure")==-1) 
                
                    his.setIndicator(ExecutionStatus.DONE);
                
                else
                    
                    his.setIndicator(ExecutionStatus.FAILED);

            }

            his.setIndicator(ExecutionStatus.DONE);
            his.setHistory_end_time(BaseTool.getCurrentSQLDate());
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

        if(!BaseTool.isNull(his)){
        
            his.setHistory_output(resp);

            his.setHistory_end_time(BaseTool.getCurrentSQLDate());

            histool.saveHistory(his);
        
        }

        sendMessageWebSocket(history_id, httpsessionid, "====== Process " + history_id + " ended");

        return resp;

    }

}
