package gw.local;

import java.io.BufferedReader;
import java.io.OutputStream;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.socket.WebSocketSession;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
/**
 * Local Session
 * @author jensensun
 *
 */
public interface LocalSession {
	
	
    public boolean login(String token, boolean isTerminal);

    public boolean logout();
    
    public boolean isTerminal();
    
	public String getHistory_process();

	public void setHistory_process(String history_process);

	public String getHistory_id();

	public void setHistory_id(String history_id);

	public BufferedReader getLocalInput();

	public OutputStream getLocalOutput();
    
	public void setWebSocketSession(WebSocketSession session);
	
	public void runBash(String script, String processid, boolean isjoin, String token);
	
	public void runJupyter(String script, String processid, boolean isjoin, String bin, String env, String basedir, String token);
	
	public void runPython(String script, String processid, boolean isjoin, String bin, String pyenv, String basedir, String token);
	
	public void runMultipleBashes(String[] script, String processid);
	
	public void saveHistory(String logs, String status);
	
	public String getToken();
	
	public String getHost() ;

	public String getPort() ;

}
