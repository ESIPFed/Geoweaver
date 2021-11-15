package com.gw.tools;

import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.gw.tasks.GeoweaverProcessTask;
import com.gw.tasks.TaskManager;

@Service
@Scope("prototype")
public class ExecutionTool {
    
    @Autowired
    ProcessTool pt;
    
	@Autowired
	LocalhostTool lt;
	
	@Autowired
	RemotehostTool rt;

    @Autowired
    BaseTool bt;

    
	@Autowired
	TaskManager tm;

    
	// @Autowired
	// GeoweaverProcessTask process_task;

    Logger logger = Logger.getLogger(this.getClass());
	
    /**
	 * Execute the process directly
	 * This method should be only called by a worker
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param httpsessionid
	 * @param isjoin
	 * @param bin
	 * @param pyenv
	 * @param basedir
	 * @return
	 */
	public String executeProcess(String history_id, String id, String hid, String pswd, String httpsessionid, 
    boolean isjoin, String bin, String pyenv, String basedir) {


        String category = pt.getTypeById(id);

        logger.debug("this process is : " + category);

        String resp = null;

        if(bt.isNull(basedir)) basedir = "~";

        if(bt.islocal(hid)) {
            
            //localhost
            if("shell".equals(category)) {
                
                resp = lt.executeShell(history_id, id, hid, pswd, httpsessionid, isjoin);
                
            }else if("builtin".equals(category)) {
                
                resp = lt.executeBuiltInProcess(history_id, id, hid, pswd, httpsessionid, isjoin);
                
            }else if("jupyter".equals(category)){
                
                resp = lt.executeJupyterProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir);
                
            }else if("python".equals(category)) {
                
                resp = lt.executePythonProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir);
                
            }else{
                
                throw new RuntimeException("This category of process is not supported");
                
            }
            
            
        }else {
            
            //non-local remote server

            if("shell".equals(category)) {
                
                resp = rt.executeShell(history_id, id, hid, pswd, httpsessionid, isjoin);
                
            }else if("builtin".equals(category)) {
                
                resp = rt.executeBuiltInProcess(history_id, id, hid, pswd, httpsessionid, isjoin, basedir);
                
            }else if("jupyter".equals(category)){
                
                resp = rt.executeJupyterProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir);
                
            }else if("python".equals(category)) {
                
                resp = rt.executePythonProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir);
                
            }else{
                
                throw new RuntimeException("This category of process is not supported");
                
            }

            
        }

        return resp;

    }

    /**
	 * Execute the process using workers
	 * This should be the method called by the controller
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param httpsessionid
	 * @param isjoin
	 * @param bin
	 * @param pyenv
	 * @param basedir
	 * @return
	 */
	// public String executeProcessByWorker(String history_id, String id, String hid, String pswd, String httpsessionid, 
    //     boolean isjoin, String bin, String pyenv, String basedir) {

    //     process_task.initialize(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir,null);
    //     tm.addANewTask(process_task);

    //     return null;

    // }

    /**
	 * Find all the available python environments on this machine
	 * @param hid
	 * @param password
	 * @return
	 */
	public String readEnvironment(String hid, String password){

		String resp = null;

		if(bt.islocal(hid)){

			resp = lt.readPythonEnvironment(hid, password);

		}else{

			resp = rt.readPythonEnvironment(hid, password);
			
		}

		return resp;

	}

}
