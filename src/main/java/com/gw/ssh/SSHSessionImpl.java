package com.gw.ssh;
/*

The MIT License (MIT)

Copyright (c) 2013 The Authors

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.PublicKey;
import java.text.Normalizer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.gw.database.HostRepository;
import com.gw.database.ProcessRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.jpa.Host;
import com.gw.server.CommandServlet;
import com.gw.tools.EnvironmentTool;
import com.gw.tools.HistoryTool;
import com.gw.utils.BaseTool;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

/**
 * Geoweaver SSH session wrapper
 * 
 * @author JensenSun
 *
 */
@Service
@Scope("prototype")
public class SSHSessionImpl implements SSHSession {

    @Autowired
    HostRepository hostrepo;

    @Autowired
    BaseTool bt;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    EnvironmentTool et;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private SSHClient ssh; // SSHJ creates a new client

    private String hostid;

    private Session session; // SSHJ client creates SSHJ session

    private Shell shell; // SSHJ session creates SSHJ shell

    private String username;

    private String token; // add by Ziheng on 12 Sep 2018 - token of each execution

    private BufferedReader input;

    private OutputStream output;

    @Value("${geoweaver.workspace}")
    private String workspace_folder_path;

    @Autowired
    private SSHLiveSessionOutput sessionsender;

    @Autowired
    private SSHCmdSessionOutput cmdsender;

    private Thread thread;

    private String host;

    private String port;

    private boolean isTerminal;

    private History history;

    @Autowired
    private HistoryTool history_tool;

    public SSHSessionImpl() {

        // this id should be passed into this class in the initilizer
        // this.history.setHistory_id(new RandomString(12).nextString()); //create a
        // history id everytime the process is executed

    }

    @Override
    public String getHistory_process() {
        return history.getHistory_process();
    }

    @Override
    public void setHistory_process(String history_process) {
        history.setHistory_process(history_process);
    }

    @Override
    public String getHistory_id() {
        return history.getHistory_id();
    }

    @Override
    public SSHClient getSsh() {
        return ssh;
    }

    @Override
    public Session getSSHJSession() {
        return session;
    }

    public void setSSHJSession(Session session) {
        this.session = session;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public boolean isTerminal() {

        return isTerminal;
    }

    public boolean login(String hostid, String password, String token, boolean isTerminal) {

        this.hostid = hostid;

        Host h = hostrepo.findById(hostid).get();

        return this.login(h.getIp(), h.getPort(), h.getUsername(), password, token, false);

    }

    @Override
    public boolean login(String host, String port, String username, String password, String token, boolean isTerminal)
            throws AuthenticationException {
        try {
            logout();
            // ssh.authPublickey(System.getProperty("user.name"));
            log.info("new SSHClient");
            ssh = new SSHClient(); // create a new SSH client
            log.info("verify all hosts");
            ssh.addHostKeyVerifier(new HostKeyVerifier() {
                public boolean verify(String arg0, int arg1, PublicKey arg2) {
                    return true; // don't bother verifying
                }

                @Override
                public List<String> findExistingAlgorithms(String hostname, int port) {
                    return null;
                }
            });
            log.info("connecting");
            ssh.connect(host, Integer.parseInt(port));
            log.info("authenticating: {}", username);
            ssh.authPassword(username, password);
            log.info("starting session");
            session = ssh.startSession(); // SSH client creates new SSH session
            log.info("allocating PTY");
            session.allocateDefaultPTY();
            this.username = username;
            this.token = token;
            this.isTerminal = isTerminal;

            if (isTerminal) {
                // shell
                log.info("starting shell");
                shell = session.startShell(); // SSH session creates SSH Shell. if shell is null, it is in command mode.
                log.info("SSH session established");
                input = new BufferedReader(new InputStreamReader(shell.getInputStream()), BaseTool.BUFFER_SIZE);
                output = shell.getOutputStream();
                // sender = new SSHSessionOutput(input, token);
                sessionsender.init(input, token);
                // moved here on 10/29/2018
                // all SSH shell sessions must have a output thread
                thread = new Thread(sessionsender);
                thread.setName("SSH output thread");
                log.info("starting sending thread");
                thread.start();
            } else {
                // command
                // do nothing here

            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            finalize();
            throw new SSHAuthenticationException(e.getMessage(), e);
        }
        return true;

    }

    @Override
    public BufferedReader getSSHInput() {
        return input;
    }

    @Override
    public OutputStream getSSHOutput() {
        return output;
    }

    @Override
    protected void finalize() {
        // stop the thread first
        try {
            if (sessionsender != null)
                sessionsender.stop();
            if (cmdsender != null)
                cmdsender.stop();
            // thread.interrupt();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            shell.close(); // sshj shell
        } catch (Throwable e) {
        }
        try {
            session.close(); // sshj session
        } catch (Throwable e) {
        }
        try {
            ssh.disconnect(); // sshj client

        } catch (Throwable e) {
        }
    }

    @Override
    public void saveHistory(String logs, String status) {

        history.setHistory_output(logs);
        
        history.setIndicator(status);
        
        this.history_tool.saveHistory(history);

    }

    public static String unaccent(String src) {
        return Normalizer
                .normalize(src, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    public static String escapeJupter(String json) {

        json = StringEscapeUtils.escapeJava(json);

        return json;

    }
	
	/**
	 * If the process ends with error
	 * @param token
	 * @param message
	 */
	public void endWithError(String token, String history_id, String message) {
		
		this.finalize();

        this.history = history_tool.getHistoryById(history_id); 
		
		this.history.setHistory_end_time(BaseTool.getCurrentSQLDate());
		
		this.history.setHistory_output(message);
		
		this.history.setIndicator(ExecutionStatus.FAILED);
		
		this.history_tool.saveHistory(this.history);

		if(!BaseTool.isNull(message))
            CommandServlet.sendMessageToSocket(token, message);
	
		CommandServlet.sendMessageToSocket(token, "The process " + this.history.getHistory_id() + " is stopped.");
	
	}

    @Override
    public void runPython(String history_id, String python, String processid, boolean isjoin, String bin, String pyenv,
            String basedir, String token) {

        try {

            log.info("starting command: " + python);

            python = escapeJupter(python);

            log.info("\n command: " + python);

            String cmdline = "mkdir -p " + workspace_folder_path +
                    "; cd " + workspace_folder_path + "; ";

            // new version of execution in which all the python files are copied in the host

            cmdline += "mkdir -p " + history_id + ";";

            cmdline += "tar -xf " + basedir + "/" + history_id + ".tar -C " +
                    workspace_folder_path + "/" + history_id + "/; ";

            cmdline += "rm -f " + basedir + "/" + history_id + ".tar; ";

            cmdline += "cd " + history_id + "/; ";

            cmdline += "chmod +x *.py;";

            String filename = processRepository.findById(processid).get().getName();

            filename = filename.trim().endsWith(".py") ? filename : filename + ".py";

            if (BaseTool.isNull(bin) || "default".equals(bin)) {

                cmdline += "python " + filename + "; ";

            } else {

                cmdline += bin + " " + filename + "; ";

            }

            cmdline += "exitcode=$?;";

            cmdline += "rm -rf " + workspace_folder_path + "/" + history_id + ";"; // remove the code

            cmdline += "exit $exitcode;";

            log.info(cmdline);

            Command cmd = session.exec(cmdline);

            log.info("SSH command session established");

            input = new BufferedReader(new InputStreamReader(cmd.getInputStream()), BaseTool.BUFFER_SIZE);

            cmdsender.init(input, token, history_id);

            // moved here on 10/29/2018
            // all SSH sessions must have a output thread

            thread = new Thread(cmdsender);

            thread.setName("SSH Command output thread");

            log.info("starting sending thread");

            thread.start();

            log.info("returning to the client..");

            if (isjoin){
                
                cmd.join(7, TimeUnit.DAYS); // longest waiting time - a week

                cmdsender.endWithCode(token, history_id, cmd.getExitStatus());

            }

        } catch (Exception e) {

            e.printStackTrace();

            // this.endWithError(token, history_id, e.getLocalizedMessage());

        }

    }

    @Override
    public void runJupyter(String history_id, String notebookjson, String processid, boolean isjoin, String bin,
            String pyenv, String basedir, String token) {

        try {

            log.info("starting command");

            String cmdline = "";

            if (!BaseTool.isNull(basedir) || "default".equals(basedir)) {

                cmdline += "cd " + basedir + "; ";

            }

            notebookjson = escapeJupter(notebookjson);

            cmdline += "echo \"" + notebookjson + "\" > jupyter-" + history_id + ".ipynb; "; // this must be changed to
                                                                                             // transfer file like the
                                                                                             // python

            if (BaseTool.isNull(bin)) {
                cmdline += "jupyter nbconvert --inplace --allow-erros --to notebook --execute jupyter-" + history_id
                        + ".ipynb;";
            } else {
                cmdline += bin + "-m jupyter nbconvert --inplace --allow-erros --to notebook --execute jupyter-"
                        + history_id + ".ipynb;";
            }

            cmdline += "exitcode=$?;";

            cmdline += "echo '*<*$$$*<*';";

            cmdline += "cat jupyter-" + history_id + ".ipynb;";

            cmdline += "echo '*>*$$$*>*';";

            cmdline += "rm -f ./jupyter-" + history_id + ".ipynb; "; // remove the script finally, leave no trace behind

            cmdline += "exit $exitcode;";

            log.info(cmdline);

            Command cmd = session.exec(cmdline);

            log.info("SSH command session established");

            input = new BufferedReader(new InputStreamReader(cmd.getInputStream()), BaseTool.BUFFER_SIZE);

            cmdsender.init(input, token, history_id);

            // moved here on 10/29/2018
            // all SSH sessions must have a output thread
            thread = new Thread(cmdsender);

            thread.setName("SSH Command output thread");

            log.info("starting sending thread");

            thread.start();

            log.info("returning to the client..");

            if (isjoin){
                
                cmd.join(7, TimeUnit.DAYS); // longest waiting time - a week
                
                cmdsender.endWithCode(token, history_id, cmd.getExitStatus());

            }

        } catch (Exception e) {

            e.printStackTrace();

            // this.endWithError(token, history_id, e.getLocalizedMessage());

        }

    }

    @Override
    public void runBash(String history_id, String script, String processid, boolean isjoin, String token) {

        try {

            log.info("starting command");

            String cmdline = "mkdir -p " + workspace_folder_path +
                    "; cd " + workspace_folder_path +
                    "; mkdir -p " + history_id +
                    "; cd " + history_id +
                    "; echo \""
                    + script.replaceAll("\r\n", "\n").replaceAll("\\$", "\\\\\\$").replaceAll("\"", "\\\\\"")
                    + "\" > geoweaver-" + history_id + ".sh; ";

            // unzip the python files into the same folder for shell to call
            cmdline += "tar -xf ~/" + history_id + ".tar -C " +
                    workspace_folder_path + "/" + history_id + "/; ";

            cmdline += "rm -f ~/" + history_id + ".tar; ";

            cmdline += "chmod +x geoweaver-" + history_id + ".sh; ";

            cmdline += "./geoweaver-" + history_id + ".sh;";

            cmdline += "exitcode=$?;";

            cmdline += "rm -rf ~/gw-workspace/" + history_id + "; "; // remove the script finally, leave no trace behind

            cmdline += "exit $exitcode;";

            log.info(cmdline);

            Command cmd = session.exec(cmdline);

            log.info("SSH command session established");

            input = new BufferedReader(new InputStreamReader(cmd.getInputStream()), BaseTool.BUFFER_SIZE);

            cmdsender.init(input, token, history_id);

            // moved here on 10/29/2018
            // all SSH sessions must have a output thread

            thread = new Thread(cmdsender);

            thread.setName("SSH Command output thread");

            log.info("starting sending thread");

            thread.start();

            log.info("returning to the client..");

            if (isjoin){
                
                cmd.join(7, TimeUnit.DAYS); // longest waiting time - a week

                cmdsender.endWithCode(token, history_id, cmd.getExitStatus());

            }

        } catch (Exception e) {

            e.printStackTrace();

            // this.endWithError(token, history_id, e.getLocalizedMessage());

        }

    }

    @Override
    public void runMultipleBashes(String history_id, String[] script, String processid) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setWebSocketSession(WebSocketSession session) {
        if (!BaseTool.isNull(sessionsender))
            this.sessionsender.setWebSocketSession(session); // connect WebSocket with SSH output thread
        if (!BaseTool.isNull(cmdsender))
            this.cmdsender.setWebSocketSession(session);
    }

    @Override
    public boolean logout() {
        log.info("logout: {}", username);
        try {
            // output.write("exit".getBytes());
            finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    public void readWhereCondaInOneCommand(String hostid) throws IOException {

        List<Environment> old_envlist = et.getEnvironmentsByHostId(hostid);

        String cmdline = "source ~/.bashrc; whereis python; conda env list";

        log.info(cmdline);

        Command cmd = session.exec(cmdline);

        String output = IOUtils.readFully(cmd.getInputStream()).toString();

        System.out.println(output);
        // An Example:
        // ## there might be some error messages here because of the source ~/.bashrc
        // python: /usr/bin/python3.6m /usr/bin/python3.6 /usr/lib/python2.7
        // /usr/lib/python3.8 /usr/lib/python3.6 /usr/lib/python3.7 /etc/python2.7
        // /etc/python /etc/python3.6 /usr/local/lib/python3.6 /usr/include/python3.6m
        // /usr/share/python
        // bash: conda: command not found
        // # conda environments:
        // #
        // /home/zsun/anaconda3
        // /home/zsun/anaconda3/envs/ag
        // base * /root/anaconda3

        String[] lines = output.split("\n");
        int nextlineindex = 1;
        // Parse "whereis python"
        for (int i = 0; i < lines.length; i++) {

            if (lines[i].startsWith("python")) {

                String pythonarraystr = lines[i].substring(8);

                String[] pythonarray = pythonarraystr.split(" ");

                for (String pypath : pythonarray) {

                    if (!BaseTool.isNull(pypath)) {

                        pypath = pypath.trim();

                        et.addNewEnvironment(pypath, old_envlist, hostid, pypath);

                    }

                }

                nextlineindex = i + 1;

                break;

            }
        }

        // parse Conda results
        if (!BaseTool.isNull(lines[nextlineindex]) && lines[nextlineindex].startsWith("# conda")) { // pass if conda is not
                                                                                              // found

            for (int i = nextlineindex + 1; i < lines.length; i++) {

                if (!lines[i].startsWith("#")) { // pass comments

                    String[] vals = lines[i].split("\\s+");

                    if (vals.length < 2)
                        continue;

                    String bin = vals[vals.length - 1] + "/bin/python"; // on linux python command is under bin folder

                    String name = BaseTool.isNull(vals[0]) ? bin : vals[0];

                    et.addNewEnvironment(bin, old_envlist, hostid, name);

                }

            }

        }

    }

    @Override
    public String readPythonEnvironment(String hostid, String password) {

        String resp = null;

        try {

            this.readWhereCondaInOneCommand(hostid);

            // this.readConda();

            resp = et.getEnvironments(hostid);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            finalize();
            // if(!BaseTool.isNull(session))
            // try {

            // session.close();
            // } catch (Exception e) {
            // e.printStackTrace();
            // }

        }

        return resp;
    }

}
