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
 * SSHSessionImpl provides an implementation of SSHSession interface for managing SSH sessions.
 * It allows for SSH connection, command execution, and managing session details.
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

    /**
     * Initiates an SSH session to a remote host for the given user and authentication details.
     *
     * @param host The hostname or IP address of the remote SSH server.
     * @param port The port number on which the SSH server is listening.
     * @param username The SSH username for authentication.
     * @param password The password for SSH authentication.
     * @param token A token associated with the session, possibly for WebSocket communication.
     * @param isTerminal A flag indicating whether this session will be an interactive shell (true) or a command-mode session (false).
     * @return True if the SSH session was successfully initiated, false otherwise.
     * @throws AuthenticationException If any authentication error occurs during the session setup.
     */
    @Override
    public boolean login(String host, String port, String username, String password, String token, boolean isTerminal)
            throws AuthenticationException {
        try {
            // Ensure that any existing session is logged out before creating a new one.
            logout();

            // Create a new SSH client instance.
            log.info("Creating a new SSHClient");
            ssh = new SSHClient();

            // Disable host key verification (Note: This can be a security risk).
            log.info("Disabling host key verification");
            ssh.addHostKeyVerifier(new HostKeyVerifier() {
                public boolean verify(String arg0, int arg1, PublicKey arg2) {
                    return true; // Don't bother verifying the host key (not recommended in production).
                }

                @Override
                public List<String> findExistingAlgorithms(String hostname, int port) {
                    return null;
                }
            });

            // Connect to the remote host using the provided hostname and port.
            log.info("Connecting to the remote host");
            ssh.connect(host, Integer.parseInt(port));

            // Authenticate using the SSH username and password.
            log.info("Authenticating as user: {}", username);
            ssh.authPassword(username, password);

            // Start an SSH session (SSH client creates a new SSH session).
            log.info("Starting SSH session");
            session = ssh.startSession();

            // Allocate a default PTY (pseudo-terminal) for the session.
            log.info("Allocating PTY");
            session.allocateDefaultPTY();

            // Set instance variables for username, token, and session type.
            this.username = username;
            this.token = token;
            this.isTerminal = isTerminal;

            if (isTerminal) {
                // If this is an interactive shell session:
                log.info("Starting shell");
                shell = session.startShell(); // SSH session creates an SSH Shell. If the shell is null, it's in command mode.
                log.info("SSH session established");
                input = new BufferedReader(new InputStreamReader(shell.getInputStream()), BaseTool.BUFFER_SIZE);
                output = shell.getOutputStream();
                sessionsender.init(input, token);

                // Create a separate thread to handle session output (SSH shell sessions require this).
                thread = new Thread(sessionsender);
                thread.setName("SSH output thread");
                log.info("Starting output sending thread");
                thread.start();
            } else {
                // If this is a command mode session, do nothing here.
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during session setup.
            e.printStackTrace();
            log.error(e.getMessage());

            // Clean up and finalize the session.
            finalize();

            // Throw an SSHAuthenticationException with the error message.
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

    /**
     * Finalizes the SSH session by stopping associated threads, closing the SSH shell and session, and disconnecting the SSH client.
     *
     * This method is called during garbage collection or when explicitly invoked for cleanup. It ensures that all resources
     * related to the SSH session are properly released and terminated.
     */
    @Override
    protected void finalize() {
        // Stop any running threads (sessionsender and cmdsender) to prevent resource leaks.
        try {
            if (sessionsender != null)
                sessionsender.stop();
            if (cmdsender != null)
                cmdsender.stop();
            // It's a good practice to stop threads to avoid resource leaks.
            // You might also consider using thread.interrupt() to gently request thread termination.
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // Close the SSH shell, if it was created.
        try {
            shell.close(); // Close the SSH shell (if it exists).
        } catch (Throwable e) {
            // Handle any exceptions or errors during shell closing.
        }

        // Close the SSH session.
        try {
            session.close(); // Close the SSH session (if it exists).
        } catch (Throwable e) {
            // Handle any exceptions or errors during session closing.
        }

        // Disconnect the SSH client to release its resources.
        try {
            ssh.disconnect(); // Disconnect the SSH client (release resources).
        } catch (Throwable e) {
            // Handle any exceptions or errors during SSH client disconnection.
        }
    }

    /**
     * Saves the execution logs and status information in the associated history object and persists it using the history_tool.
     *
     * @param logs   The execution logs to be saved.
     * @param status The status indicator (e.g., "SUCCEEDED," "FAILED") of the execution.
     */
    @Override
    public void saveHistory(String logs, String status) {
        // Set the execution logs and status in the history object.
        history.setHistory_output(logs);
        history.setIndicator(status);
        // Save the history using the history_tool, which persists the history information.
        this.history_tool.saveHistory(history);
    }


    /**
     * Removes diacritical marks (accents) from the input text and returns the unaccented string.
     *
     * @param src The input text containing accented characters.
     * @return The input text with diacritical marks removed.
     */
    public static String unaccent(String src) {
        return Normalizer
                .normalize(src, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }


    /**
     * Escapes special characters in a JSON string to ensure its safe usage in a Jupyter notebook.
     *
     * @param json The JSON string to be escaped.
     * @return An escaped JSON string suitable for use in a Jupyter notebook.
     */
    public static String escapeJupter(String json) {
        // Use StringEscapeUtils to escape special characters in the JSON string.
        json = StringEscapeUtils.escapeJava(json);
        return json;
    }

	/**
     * Ends the current SSH session with an error status and records relevant information in the associated history object.
     *
     * @param token      The token associated with the session.
     * @param history_id The identifier of the associated history.
     * @param message    The error message or log to be recorded.
     */
    public void endWithError(String token, String history_id, String message) {
        // Terminate the current SSH session.
        this.finalize();
        
        // Retrieve the history object associated with the given history_id.
        this.history = history_tool.getHistoryById(history_id); 

        // Set the end time of the history to the current date and time.
        this.history.setHistory_end_time(BaseTool.getCurrentSQLDate());

        // Set the execution logs to include the provided error message.
        this.history.setHistory_output(message);

        // Set the execution status indicator to "FAILED."
        this.history.setIndicator(ExecutionStatus.FAILED);

        // Persist the updated history object using the history_tool.
        this.history_tool.saveHistory(this.history);

        // If the error message is not empty, send it to the associated socket.
        if (!BaseTool.isNull(message)) {
            CommandServlet.sendMessageToSocket(token, message);
        }

        // Send a message to the socket indicating that the process is stopped.
        CommandServlet.sendMessageToSocket(token, "The process " + this.history.getHistory_id() + " is stopped.");
    }


    /**
     * Execute a Python script on the SSH session and handle the associated process.
     *
     * @param history_id The identifier of the associated history.
     * @param python     The Python script to be executed.
     * @param processid  The identifier of the associated process.
     * @param isjoin     A boolean flag indicating whether to wait for the process to complete.
     * @param bin        The path to the Python binary (interpreter) to be used.
     * @param pyenv      The Python environment (if provided).
     * @param basedir    The base directory for execution.
     * @param token      The token associated with the session.
     */
    @Override
    public void runPython(String history_id, String python, String processid, boolean isjoin, String bin, String pyenv,
            String basedir, String token) {
        try {
            // Log that the Python script execution is starting.
            log.info("Starting command: " + python);

            // Escape any special characters in the Python script.
            python = escapeJupter(python);

            // Log the processed Python script.
            log.info("\nCommand: " + python);

            // Build the command line for execution.
            String cmdline = "mkdir -p " + workspace_folder_path +
                    "; cd " + workspace_folder_path + "; ";

            // Create a directory for the execution and extract Python files.
            cmdline += "mkdir -p " + history_id + ";";
            cmdline += "tar -xf " + basedir + "/" + history_id + ".tar -C " +
                    workspace_folder_path + "/" + history_id + "/; ";
            cmdline += "rm -f " + basedir + "/" + history_id + ".tar; ";
            cmdline += "cd " + history_id + "/; ";

            // Make Python script files executable.
            cmdline += "chmod +x *.py;";

            // Determine the filename for the Python script.
            String filename = processRepository.findById(processid).get().getName();
            filename = filename.trim().endsWith(".py") ? filename : filename + ".py";

            // Build the Python execution command.
            if (BaseTool.isNull(bin) || "default".equals(bin)) {
                cmdline += "python -u " + filename + "; ";
            } else {
                cmdline += bin + " -u " + filename + "; ";
            }

            // Set the exit code to the result of the executed Python script.
            cmdline += "exitcode=$;";

            // Remove the executed code directory to clean up.
            cmdline += "rm -rf " + workspace_folder_path + "/" + history_id + ";";

            // Complete command line setup.
            cmdline += "exit $exitcode;";

            // Log the final command line.
            log.info(cmdline);

            // Execute the command on the SSH session.
            Command cmd = session.exec(cmdline);

            // Log the establishment of the SSH command session.
            log.info("SSH command session established");

            // Initialize an input stream for reading command output.
            input = new BufferedReader(new InputStreamReader(cmd.getInputStream()), BaseTool.BUFFER_SIZE);

            // Initialize the command sender for handling output.
            cmdsender.init(input, token, history_id);

            // Create and start a thread for sending command output.
            thread = new Thread(cmdsender);
            thread.setName("SSH Command output thread");
            log.info("Starting sending thread");
            thread.start();

            // Return to the client after starting the execution.

            // If the 'isjoin' flag is set, wait for the process to complete.
            if (isjoin){
                cmd.join(7, TimeUnit.DAYS); // Allow the process to run for up to a week.
                cmdsender.endWithCode(token, history_id, cmd.getExitStatus());
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during execution.
            e.printStackTrace();
            // Alternatively, you can call the 'endWithError' method here to handle errors.
            // this.endWithError(token, history_id, e.getLocalizedMessage());
        }
    }


    /**
     * Execute a Jupyter Notebook using a specified JSON configuration on the SSH session and manage the associated process.
     *
     * @param history_id    The identifier of the associated history.
     * @param notebookjson  The Jupyter Notebook configuration in JSON format.
     * @param processid     The identifier of the associated process.
     * @param isjoin        A boolean flag indicating whether to wait for the process to complete.
     * @param bin           The path to the Python binary (interpreter) to be used.
     * @param pyenv         The Python environment (if provided).
     * @param basedir       The base directory for execution.
     * @param token         The token associated with the session.
     */
    @Override
    public void runJupyter(String history_id, String notebookjson, String processid, boolean isjoin, String bin,
            String pyenv, String basedir, String token) {
        try {
            // Log that the Jupyter Notebook execution is starting.
            log.info("Starting command");

            // Create an empty command line.
            String cmdline = "";

            // If a base directory is specified, change to that directory.
            if (!BaseTool.isNull(basedir) || "default".equals(basedir)) {
                cmdline += "cd " + basedir + "; ";
            }

            // Escape any special characters in the Jupyter Notebook JSON.
            notebookjson = escapeJupter(notebookjson);

            // Create the Jupyter Notebook file.
            cmdline += "echo \"" + notebookjson + "\" > jupyter-" + history_id + ".ipynb; ";

            // Determine the command to execute Jupyter Notebook with the specified options.
            if (BaseTool.isNull(bin)) {
                cmdline += "jupyter nbconvert --inplace --allow-erros --to notebook --execute jupyter-" + history_id + ".ipynb;";
            } else {
                cmdline += bin + "-m jupyter nbconvert --inplace --allow-erros --to notebook --execute jupyter-" + history_id + ".ipynb;";
            }

            // Set the exit code to the result of the executed Jupyter Notebook.
            cmdline += "exitcode=$;";

            // Include markers to identify the start and end of Jupyter Notebook output.
            cmdline += "echo '*<*$$$*<*';";
            cmdline += "cat jupyter-" + history_id + ".ipynb;";
            cmdline += "echo '*>*$$$*>*';";

            // Remove the Jupyter Notebook script, leaving no trace behind.
            cmdline += "rm -f ./jupyter-" + history_id + ".ipynb;";

            // Complete command line setup.
            cmdline += "exit $exitcode;";

            // Log the final command line.
            log.info(cmdline);

            // Execute the command on the SSH session.
            Command cmd = session.exec(cmdline);

            // Log the establishment of the SSH command session.
            log.info("SSH command session established");

            // Initialize an input stream for reading command output.
            input = new BufferedReader(new InputStreamReader(cmd.getInputStream()), BaseTool.BUFFER_SIZE);

            // Initialize the command sender for handling output.
            cmdsender.init(input, token, history_id);

            // Create and start a thread for sending command output.
            thread = new Thread(cmdsender);
            thread.setName("SSH Command output thread");
            log.info("Starting sending thread");

            thread.start();

            // Return to the client after starting the execution.

            // If the 'isjoin' flag is set, wait for the process to complete.
            if (isjoin) {
                cmd.join(7, TimeUnit.DAYS); // Allow the process to run for up to a week.
                cmdsender.endWithCode(token, history_id, cmd.getExitStatus());
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during execution.
            e.printStackTrace();
            // Alternatively, you can call the 'endWithError' method here to handle errors.
            // this.endWithError(token, history_id, e.getLocalizedMessage());
        }
    }

    /**
     * Execute a Bash script on the SSH session and manage the associated process.
     *
     * @param history_id   The identifier of the associated history.
     * @param script       The Bash script to be executed.
     * @param processid    The identifier of the associated process.
     * @param isjoin       A boolean flag indicating whether to wait for the process to complete.
     * @param token        The token associated with the session.
     */
    @Override
    public void runBash(String history_id, String script, String processid, boolean isjoin, String token) {
        try {
            // Log that the Bash script execution is starting.
            log.info("Starting command");

            // Create an empty command line.
            String cmdline = "mkdir -p " + workspace_folder_path + "; cd " + workspace_folder_path + "; ";

            // Set up the execution directory for the Bash script.
            cmdline += "mkdir -p " + history_id + "; cd " + history_id + "; ";

            // Create the Bash script file.
            cmdline += "echo \"" + script
                    .replaceAll("\r\n", "\n")
                    .replaceAll("\\$", "\\\\\\$")
                    .replaceAll("\"", "\\\\\"")
                    + "\" > geoweaver-" + history_id + ".sh; ";

            // Unzip any Python files into the execution folder for the script to use.
            cmdline += "tar -xf ~/" + history_id + ".tar -C " +
                    workspace_folder_path + "/" + history_id + "/; ";

            // Remove the Python files archive.
            cmdline += "rm -f ~/" + history_id + ".tar; ";

            // Make the Bash script executable.
            cmdline += "chmod +x geoweaver-" + history_id + ".sh; ";

            // Execute the Bash script.
            cmdline += "./geoweaver-" + history_id + ".sh;";

            // Set the exit code to the result of the executed Bash script.
            cmdline += "exitcode=$;";

            // Remove the execution folder, leaving no trace behind.
            cmdline += "rm -rf ~/gw-workspace/" + history_id + "; ";

            // Complete command line setup.
            cmdline += "exit $exitcode;";

            // Log the final command line.
            log.info(cmdline);

            // Execute the command on the SSH session.
            Command cmd = session.exec(cmdline);

            // Log the establishment of the SSH command session.
            log.info("SSH command session established");

            // Initialize an input stream for reading command output.
            input = new BufferedReader(new InputStreamReader(cmd.getInputStream()), BaseTool.BUFFER_SIZE);

            // Initialize the command sender for handling output.
            cmdsender.init(input, token, history_id);

            // Create and start a thread for sending command output.
            thread = new Thread(cmdsender);
            thread.setName("SSH Command output thread");
            log.info("Starting sending thread");

            thread.start();

            // Return to the client after starting the execution.

            // If the 'isjoin' flag is set, wait for the process to complete.
            if (isjoin) {
                cmd.join(7, TimeUnit.DAYS); // Allow the process to run for up to a week.
                cmdsender.endWithCode(token, history_id, cmd.getExitStatus());
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during execution.
            e.printStackTrace();
            // Alternatively, you can call the 'endWithError' method here to handle errors.
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

    /**
     * Read information about Python and Conda environments on a remote host in one command.
     * This method executes a command on the SSH session to get information about Python and Conda environments.
     *
     * @param hostid The identifier of the remote host.
     * @throws IOException If an I/O error occurs while reading the command output.
     */
    public void readWhereCondaInOneCommand(String hostid) throws IOException {
        try {
            // Get the list of existing environments associated with the host.
            List<Environment> old_envlist = et.getEnvironmentsByHostId(hostid);

            // Define the command to be executed.
            String cmdline = "source ~/.bashrc; whereis python; conda env list";

            // Log the command for debugging purposes.
            log.info(cmdline);

            // Execute the command on the SSH session.
            Command cmd = session.exec(cmdline);

            // Read the command's output into a string.
            String output = IOUtils.readFully(cmd.getInputStream()).toString();

            // Print the output to the console or log it for debugging.
            System.out.println(output);

            // Split the output into lines.
            String[] lines = output.split("\n");
            int nextlineindex = 1;

            // Parse the output of "whereis python."
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].startsWith("python")) {
                    // Extract paths where Python is located.
                    String pythonarraystr = lines[i].substring(8);
                    String[] pythonarray = pythonarraystr.split(" ");

                    // Add Python environments to the environment list.
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

            // Parse the output of Conda environments.
            if (!BaseTool.isNull(lines[nextlineindex]) && lines[nextlineindex].startsWith("# conda")) {
                // Iterate through the lines, skipping comments.
                for (int i = nextlineindex + 1; i < lines.length; i++) {
                    if (!lines[i].startsWith("#")) {
                        String[] vals = lines[i].split("\\s+");

                        if (vals.length < 2)
                            continue;

                        // Extract the path to the Conda environment's Python binary.
                        String bin = vals[vals.length - 1] + "/bin/python";

                        // Determine the name for the environment (defaulting to the binary path if no name is provided).
                        String name = BaseTool.isNull(vals[0]) ? bin : vals[0];

                        // Add the Conda environment to the environment list.
                        et.addNewEnvironment(bin, old_envlist, hostid, name);
                    }
                }
            }
        } catch (IOException e) {
            // Handle any exceptions that occur during execution.
            e.printStackTrace();
            // You may want to add additional error handling here based on your use case.
        }
    }

    /**
     * Read Python and Conda environments information on a remote host and return it as a formatted string.
     * This method reads information about Python and Conda environments by executing remote commands.
     * The collected environment information is returned as a formatted string.
     *
     * @param hostid   The identifier of the remote host.
     * @param password The password for the SSH session.
     * @return A formatted string containing information about Python and Conda environments on the remote host.
     */
    @Override
    public String readPythonEnvironment(String hostid, String password) {
        String resp = null;

        try {
            // Read information about Python and Conda environments in one command.
            readWhereCondaInOneCommand(hostid);

            // Read additional Conda environment information if needed.
            // this.readConda();

            // Get a formatted string containing the environment information.
            resp = et.getEnvironments(hostid);
        } catch (Exception e) {
            // Handle any exceptions that occur during the environment reading process.
            e.printStackTrace();
            // You may want to add additional error handling here based on your use case.
        } finally {
            // Perform cleanup and finalization.
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
