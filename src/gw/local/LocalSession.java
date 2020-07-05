package gw.local;

import java.io.BufferedReader;
import java.io.OutputStream;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.socket.WebSocketSession;

import gw.log.History;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
/**
 * Local Session
 * @author jensensun
 *
 */
public interface LocalSession {
	
    public boolean isTerminal();
    
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
	public void runBash(String script, String processid, boolean isjoin, String token);
	
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
	public void runJupyter(String script, String processid, boolean isjoin, String bin, String env, String basedir, String token);
	
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
	public void runPython(String script, String processid, boolean isjoin, String bin, String pyenv, String basedir, String token);
	
	public void runMultipleBashes(String[] script, String processid);
	/**
	 * Save history to database
	 * @param logs
	 * @param status
	 */
	public void saveHistory(String logs, String status);
	
}
