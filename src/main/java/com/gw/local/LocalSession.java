package com.gw.local;

import java.io.BufferedReader;

import com.gw.jpa.History;
/**
 * Local Session
 * @author jensensun
 *
 */
public interface LocalSession {
	
    public boolean isTerminal();

	/**
	 * Check if this session is closed. 
	 * Notice: only work for process that has waitFor enabled. the process without join doesn't work.
	 * @return
	 * close - true; open - false
	 */
	public boolean isClose(); 
    
	public History getHistory();

	public void setHistory(History history);

	public String getToken();
	
	public BufferedReader getLocalInput();
	
	public boolean clean();
	
	/**
	 * Stop all the ongoing commands and tasks
	 * @return
	 */
	public boolean stop();

	/**
	 * Find all python environments on the localhost
	 * @return
	 */
	public String readPythonEnvironment(String hostid, String password);
	
//	public void setWebSocketSession(WebSocketSession session);
	/**
	 * Run bash script
	 * @param script
	 * @param processid
	 * @param isjoin
	 * is terminal or not
	 * @param token
	 * http session id
	 */
	public void runBash(String history_id, String script, String processid, boolean isjoin, String token);
	
	/**
	 * Run jupyter locally
	 * @param script
	 * @param processid
	 * @param isjoin
	 * @param bin
	 * @param env
	 * @param basedir
	 * @param token
	 * http session id
	 */
	public void runJupyter(String history_id, String script, String processid, boolean isjoin, String bin, String env, String basedir, String token);
	
	/**
	 * Run Python locally
	 * @param script
	 * @param processid
	 * @param isjoin
	 * @param bin
	 * @param pyenv
	 * @param basedir
	 * @param token
	 * http session id
	 */
	public void runPython(String history_id, String script, String processid, boolean isjoin, String bin, String pyenv, String basedir, String token);
	
	public void runMultipleBashes(String history_id, String[] script, String processid);
	/**
	 * Save history to database
	 * @param logs
	 * @param status
	 */
	public void saveHistory(String logs, String status);
	
}
