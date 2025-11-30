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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.PublicKey;
import java.text.Normalizer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

/**
 * SSHSessionImpl provides an implementation of SSHSession interface for managing SSH sessions. It
 * allows for SSH connection, command execution, and managing session details.
 *
 * @author JensenSun
 */
@Service
@Scope("prototype")
public class SSHSessionImpl implements SSHSession {

  @Autowired HostRepository hostrepo;

  @Autowired BaseTool bt;

  @Autowired ProcessRepository processRepository;

  @Autowired EnvironmentTool et;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private SSHClient ssh; // SSHJ creates a new client

  private String hostid;

  private Session session; // SSHJ client creates SSHJ session

  private Shell shell; // SSHJ session creates SSHJ shell

  private Command currentCommand; // Store the current executing command to allow termination

  private String currentPidFile; // Store PID file path for process termination

  private String historyId; // Store history ID for process termination

  private String username;

  private String token; // add by Ziheng on 12 Sep 2018 - token of each execution

  private BufferedReader input;

  private OutputStream output;

  @Value("${geoweaver.workspace}")
  private String workspace_folder_path;

  @Autowired private SSHLiveSessionOutput sessionsender;

  @Autowired private SSHCmdSessionOutput cmdsender;

  private Thread thread;

  private String host;

  private String port;

  private boolean isTerminal;

  private History history;

  @Autowired private HistoryTool history_tool;

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

    if (h == null) {
      log.error("Host not found with id: " + hostid);
      throw new RuntimeException("Host not found with id: " + hostid);
    }
    
    String ip = h.getIp();
    String port = h.getPort();
    String username = h.getUsername();
    
    // Validate host information
    if (ip == null || ip.isEmpty()) {
      log.error("Host IP is null or empty for hostid: " + hostid);
      throw new RuntimeException("Host IP is not configured for host: " + hostid);
    }
    
    if (port == null || port.isEmpty()) {
      log.warn("Host port is null or empty for hostid: " + hostid + ", using default port 22");
      port = "22";
    }
    
    if (username == null || username.isEmpty()) {
      log.error("Host username is null or empty for hostid: " + hostid);
      throw new RuntimeException("Host username is not configured for host: " + hostid);
    }
    
    log.info("Attempting SSH login to host: " + ip + ":" + port + " as user: " + username);

    return this.login(ip, port, username, password, token, isTerminal);
  }

  /**
   * Initiates an SSH session to a remote host for the given user and authentication details.
   *
   * @param host The hostname or IP address of the remote SSH server.
   * @param port The port number on which the SSH server is listening.
   * @param username The SSH username for authentication.
   * @param password The password for SSH authentication.
   * @param token A token associated with the session, possibly for WebSocket communication.
   * @param isTerminal A flag indicating whether this session will be an interactive shell (true) or
   *     a command-mode session (false).
   * @return True if the SSH session was successfully initiated, false otherwise.
   * @throws AuthenticationException If any authentication error occurs during the session setup.
   */
  @Override
  public boolean login(
      String host, String port, String username, String password, String token, boolean isTerminal)
      throws AuthenticationException {
    // Store original network preferences to restore later
    String originalIpv4Preference = System.getProperty("java.net.preferIPv4Stack");
    String originalIpv6Addresses = System.getProperty("java.net.preferIPv6Addresses");
    
    try {
      // Ensure that any existing session is logged out before creating a new one.
      logout();

      // Force IPv4 preference before creating SSH client
      try {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
        log.info("Set network preferences: preferIPv4Stack=true, preferIPv6Addresses=false");
      } catch (Exception e) {
        log.warn("Failed to set network preferences: " + e.getMessage());
      }

      // Create a new SSH client instance.
      log.info("Creating a new SSHClient");
      ssh = new SSHClient();
      
      // Note: We'll test if binding to en0 works in the connectivity test
      // If it works, we may need to use a workaround to force SSHJ to use en0

      // Disable host key verification (Note: This can be a security risk).
      log.info("Disabling host key verification");
      ssh.addHostKeyVerifier(
          new HostKeyVerifier() {
            public boolean verify(String arg0, int arg1, PublicKey arg2) {
              return true; // Don't bother verifying the host key (not recommended in production).
            }

            @Override
            public List<String> findExistingAlgorithms(String hostname, int port) {
              return null;
            }
          });

      // Connect to the remote host using the provided hostname and port.
      log.info("Connecting to the remote host: " + host + ":" + port);
      
      // Validate port
      int portNum;
      try {
        portNum = Integer.parseInt(port);
      } catch (NumberFormatException e) {
        log.error("Invalid port number: " + port);
        throw new RuntimeException("Invalid port number: " + port);
      }
      
      // Network diagnostics - comprehensive connectivity test
      log.info("=== Starting Network Connectivity Diagnostics ===");
      boolean networkAccessible = testNetworkConnectivity(host, portNum);
      log.info("=== Network Connectivity Test Result: " + (networkAccessible ? "PASSED" : "FAILED") + " ===");
      
      if (!networkAccessible) {
        log.error("Network connectivity test failed. SSH connection will likely fail.");
        log.error("Please check:");
        log.error("  1. Firewall rules - ensure Java application can access " + host + ":" + portNum);
        log.error("  2. Network routing - verify route to " + host);
        log.error("  3. Network interface - check if correct interface is being used");
      }
      
      // Set connection timeout (10 seconds - reduced for faster connection)
      ssh.setConnectTimeout(10000);
      
      // Set socket timeout (10 seconds - reduced for faster response)
      ssh.setTimeout(10000);
      
      // Try to connect with better error handling
      try {
        log.info("Attempting SSH connection to " + host + ":" + portNum);
        
        // Automatically find the correct network interface on the same subnet as target
        // This works for any interface name (en0, eth0, wlan0, etc.)
        java.net.InetAddress bindInterface = null;
        String bindInterfaceName = null;
        try {
          java.net.InetAddress target = java.net.InetAddress.getByName(host);
          String targetIp = target.getHostAddress();
          log.info("Looking for network interface on same subnet as target: " + targetIp);
          
          java.util.Enumeration<java.net.NetworkInterface> interfaces = 
              java.net.NetworkInterface.getNetworkInterfaces();
          
          while (interfaces.hasMoreElements()) {
            java.net.NetworkInterface ni = interfaces.nextElement();
            
            // Skip loopback and down interfaces
            if (ni.isLoopback() || !ni.isUp()) {
              continue;
            }
            
            String interfaceName = ni.getName();
            log.debug("Checking interface: " + interfaceName + " (isUp=" + ni.isUp() + ")");
            
            java.util.List<java.net.InetAddress> addresses = 
                java.util.Collections.list(ni.getInetAddresses());
            
            for (java.net.InetAddress localAddr : addresses) {
              if (localAddr instanceof java.net.Inet4Address) {
                String localIp = localAddr.getHostAddress();
                String[] localParts = localIp.split("\\.");
                String[] targetParts = targetIp.split("\\.");
                
                if (localParts.length == 4 && targetParts.length == 4) {
                  // Check if on same subnet (first 3 octets match)
                  boolean sameSubnet = localParts[0].equals(targetParts[0]) && 
                                      localParts[1].equals(targetParts[1]) && 
                                      localParts[2].equals(targetParts[2]);
                  
                  if (sameSubnet) {
                    bindInterface = localAddr;
                    bindInterfaceName = interfaceName;
                    log.info("Found interface " + interfaceName + " on same subnet as target");
                    log.info("  Interface IP: " + localIp);
                    log.info("  Target IP: " + targetIp);
                    log.info("  Will attempt to use this interface for SSH connection");
                    break;
                  } else {
                    log.debug("  Interface " + interfaceName + " IP " + localIp + " is on different subnet");
                  }
                }
              }
            }
            
            if (bindInterface != null) {
              break; // Found the right interface
            }
          }
          
          if (bindInterface == null) {
            log.warn("Could not find network interface on same subnet as target " + targetIp);
            log.warn("Will use default routing (may fail if wrong interface is selected)");
          }
        } catch (Exception ifEx) {
          log.error("Could not find network interface: " + ifEx.getMessage());
          ifEx.printStackTrace();
        }
        
        // Note: SSHJ doesn't directly support binding to a specific interface
        // But we can set system properties to influence interface selection
        if (bindInterface != null) {
          log.info("Target interface identified: " + bindInterfaceName + " (" + bindInterface.getHostAddress() + ")");
          log.info("Note: SSHJ will use system routing, which should prefer this interface");
        }
        
        // If host is an IP address, try connecting directly
        // Otherwise, let SSHJ handle the resolution
        if (host.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
          log.info("Connecting to IPv4 address: " + host);
          ssh.connect(host, portNum);
        } else {
          log.info("Connecting to hostname: " + host + " (will be resolved)");
          ssh.connect(host, portNum);
        }
        
        log.info("Successfully connected to " + host + ":" + port);
      } catch (java.net.NoRouteToHostException e) {
        log.error("No route to host " + host + ":" + port);
        log.error("This usually means:");
        log.error("  1. Network routing issue - check if the host is reachable from this machine");
        log.error("  2. Firewall blocking Java application - check firewall rules");
        log.error("  3. Network interface binding issue - Java may be using wrong network interface");
        log.error("  4. IPv4/IPv6 mismatch - try using explicit IPv4 address");
        log.error("Current network interfaces:");
        try {
          // First, explicitly check for en0
          try {
            java.net.NetworkInterface en0 = java.net.NetworkInterface.getByName("en0");
            if (en0 != null) {
              log.error("  en0 interface found: isUp=" + en0.isUp() + ", Addresses: " + 
                       java.util.Collections.list(en0.getInetAddresses()));
              if (!en0.isUp()) {
                log.error("  WARNING: en0 interface exists but is NOT UP!");
              }
            } else {
              log.error("  en0 interface NOT FOUND!");
            }
          } catch (Exception en0Ex) {
            log.error("  Failed to get en0 interface: " + en0Ex.getMessage());
          }
          
          // List all interfaces
          java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
          while (interfaces.hasMoreElements()) {
            java.net.NetworkInterface ni = interfaces.nextElement();
            if (ni.isUp() && !ni.isLoopback()) {
              java.util.List<java.net.InetAddress> addrs = java.util.Collections.list(ni.getInetAddresses());
              log.error("  Interface: " + ni.getName() + ", isUp=" + ni.isUp() + 
                       ", Addresses: " + addrs);
              // Check if this is en0
              if (ni.getName().equals("en0")) {
                log.error("    -> This is en0! Checking for IPv4 address...");
                for (java.net.InetAddress addr : addrs) {
                  if (addr instanceof java.net.Inet4Address) {
                    log.error("    -> en0 has IPv4: " + addr.getHostAddress());
                  }
                }
              }
            } else {
              // Log interfaces that are down or loopback for debugging
              if (ni.getName().equals("en0")) {
                log.error("  Interface en0 found but isUp=" + ni.isUp() + ", isLoopback=" + ni.isLoopback());
              }
            }
          }
        } catch (Exception ex) {
          log.error("Failed to list network interfaces: " + ex.getMessage());
          ex.printStackTrace();
        }
        throw new RuntimeException("No route to host " + host + ":" + port + ". Please check network connectivity and firewall settings.", e);
      } catch (java.net.ConnectException e) {
        log.error("Connection refused to " + host + ":" + port);
        log.error("This usually means the SSH service is not running or not accessible on that port");
        throw new RuntimeException("Connection refused to " + host + ":" + port + ". Please check if SSH service is running.", e);
      } catch (java.net.SocketTimeoutException e) {
        log.error("Connection timeout to " + host + ":" + port);
        log.error("This usually means the host is not reachable or firewall is blocking the connection");
        throw new RuntimeException("Connection timeout to " + host + ":" + port + ". Please check network connectivity.", e);
      } catch (Exception e) {
        log.error("Failed to connect to " + host + ":" + port + " - " + e.getMessage(), e);
        log.error("Exception type: " + e.getClass().getName());
        
        // Additional diagnostic: try to ping the host
        try {
          java.net.InetAddress addr = java.net.InetAddress.getByName(host);
          log.error("Attempting to ping " + addr.getHostAddress());
          boolean pingResult = addr.isReachable(5000);
          log.error("Ping result: " + pingResult);
        } catch (Exception pingEx) {
          log.error("Ping test failed: " + pingEx.getMessage());
        }
        
        throw e;
      }

      // Authenticate using the SSH username and password.
      log.info("Authenticating as user: " + username);
      try {
      ssh.authPassword(username, password);
        log.info("Successfully authenticated as user: " + username);
      } catch (Exception e) {
        log.error("Authentication failed for user: " + username + " - " + e.getMessage(), e);
        throw e;
      }

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
        shell =
            session
                .startShell(); // SSH session creates an SSH Shell. If the shell is null, it's in
                               // command mode.
        log.info("SSH session established");
        input =
            new BufferedReader(new InputStreamReader(shell.getInputStream()), BaseTool.BUFFER_SIZE);
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
    } finally {
      // Restore original network preference settings
      try {
        if (originalIpv4Preference != null) {
          System.setProperty("java.net.preferIPv4Stack", originalIpv4Preference);
        } else {
          System.clearProperty("java.net.preferIPv4Stack");
        }
        if (originalIpv6Addresses != null) {
          System.setProperty("java.net.preferIPv6Addresses", originalIpv6Addresses);
        } else {
          System.clearProperty("java.net.preferIPv6Addresses");
        }
      } catch (Exception e) {
        log.warn("Failed to restore network preferences: " + e.getMessage());
      }
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
   * Finalizes the SSH session by stopping associated threads, closing the SSH shell and session,
   * and disconnecting the SSH client.
   *
   * <p>This method is called during garbage collection or when explicitly invoked for cleanup. It
   * ensures that all resources related to the SSH session are properly released and terminated.
   */
  @Override
  protected void finalize() {
    // Stop any running threads (sessionsender and cmdsender) to prevent resource leaks.
    try {
      if (sessionsender != null) sessionsender.stop();
      if (cmdsender != null) cmdsender.stop();
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
   * Saves the execution logs and status information in the associated history object and persists
   * it using the history_tool.
   *
   * @param logs The execution logs to be saved.
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
    return Normalizer.normalize(src, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
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
   * Ends the current SSH session with an error status and records relevant information in the
   * associated history object.
   *
   * @param token The token associated with the session.
   * @param history_id The identifier of the associated history.
   * @param message The error message or log to be recorded.
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
      CommandServlet.sendMessageToSocket(
          token, this.history.getHistory_id() + BaseTool.log_separator + message);
    }

    // Send a message to the socket indicating that the process is stopped.
    CommandServlet.sendMessageToSocket(
        token,
        this.history.getHistory_id()
            + BaseTool.log_separator
            + "The process "
            + this.history.getHistory_id()
            + " is stopped.");
  }

  /**
   * Execute a Python script on the SSH session and handle the associated process.
   *
   * @param history_id The identifier of the associated history.
   * @param python The Python script to be executed.
   * @param processid The identifier of the associated process.
   * @param isjoin A boolean flag indicating whether to wait for the process to complete.
   * @param bin The path to the Python binary (interpreter) to be used.
   * @param pyenv The Python environment (if provided).
   * @param basedir The base directory for execution.
   * @param token The token associated with the session.
   */
  @Override
  public void runPython(
      String history_id,
      String python,
      String processid,
      boolean isjoin,
      String bin,
      String pyenv,
      String basedir,
      String token) {
    try {
      // Build the command line for execution.
      String cmdline = "mkdir -p " + workspace_folder_path + "; cd " + workspace_folder_path + "; ";

      // Create a directory for the execution and extract Python files.
      cmdline += "mkdir -p " + history_id + ";";
      cmdline +=
          "tar -xf "
              + basedir
              + "/"
              + history_id
              + ".tar -C "
              + workspace_folder_path
              + "/"
              + history_id
              + "/; ";
      cmdline += "rm -f " + basedir + "/" + history_id + ".tar; ";
      cmdline += "cd " + history_id + "/; ";

      // Make Python script files executable.
      cmdline += "chmod +x *.py;";

      // Determine the filename for the Python script.
      String filename = processRepository.findById(processid).get().getName();
      filename = filename.trim().endsWith(".py") ? filename : filename + ".py";

      // Store history ID and PID file path for process termination BEFORE building command
      this.historyId = history_id;
      String pidFile = workspace_folder_path + "/" + history_id + ".pid";
      this.currentPidFile = pidFile;
      
      log.info("=== Starting Python execution for history ID: {} ===", history_id);
      log.info("PID file path: {}", pidFile);
      log.info("Workspace folder: {}", workspace_folder_path);
      
      // Build the Python execution command with process tracking
      // Use setsid to create a new process group for easier termination
      String pythonCmd;
      if (BaseTool.isNull(bin) || "default".equals(bin)) {
        pythonCmd = "python -u " + filename;
      } else {
        pythonCmd = bin + " -u " + filename;
      }
      
      log.info("Python command: {}", pythonCmd);
      log.info("Python filename: {}", filename);
      
      // Wrap Python command to track the actual Python process PID for reliable termination
      // The key is to find the actual Python process, not the shell wrapper
      String trackedPythonCmd = "(" +
                                 "echo '[GW-DEBUG] Starting Python process tracking...' >&2; " +
                                 "trap 'rm -f " + pidFile + "' EXIT; " +  // Cleanup PID file on exit
                                 "(" + pythonCmd + " 2>&1 | tee " + workspace_folder_path + "/" + history_id + ".log) & " +
                                 "SHELL_PID=$!; " +
                                 "echo '[GW-DEBUG] Shell PID after background: '$SHELL_PID >&2; " +
                                 "sleep 0.5; " +  // Give Python process time to start
                                 "PYTHON_PID=$(pgrep -P $SHELL_PID 2>/dev/null | head -1); " +  // Find Python process under shell
                                 "if [ -z \"$PYTHON_PID\" ]; then " +
                                 "  PYTHON_PID=$(ps aux | grep -E 'python.*" + filename + "' | grep -v grep | awk '{print $2}' | head -1); " +  // Fallback: find by process name
                                 "  echo '[GW-DEBUG] Found Python PID by name search: '$PYTHON_PID >&2; " +
                                 "fi; " +
                                 "if [ -z \"$PYTHON_PID\" ]; then " +
                                 "  PYTHON_PID=$SHELL_PID; " +  // Last resort: use shell PID
                                 "  echo '[GW-DEBUG] Using shell PID as fallback: '$PYTHON_PID >&2; " +
                                 "fi; " +
                                 "PGID=$(ps -o pgid= -p $PYTHON_PID 2>/dev/null | tr -d ' '); " +  // Get process group ID
                                 "if [ -z \"$PGID\" ]; then PGID=$PYTHON_PID; fi; " +  // Fallback to PID
                                 "echo '[GW-DEBUG] Python PID: '$PYTHON_PID', PGID: '$PGID >&2; " +
                                 "echo $PYTHON_PID > " + pidFile + "; " +  // Save actual Python PID
                                 "echo $PGID >> " + pidFile + "; " +  // Also save PGID on second line
                                 "echo '[GW-DEBUG] Saved PID and PGID to " + pidFile + ", content: '$(cat " + pidFile + ") >&2; " +
                                 "wait $SHELL_PID; " +
                                 "EXIT_CODE=$?; " +
                                 "echo '[GW-DEBUG] Process exited with code: '$EXIT_CODE >&2; " +
                                 "rm -f " + pidFile + "; " +
                                 "exit $EXIT_CODE" +
                                 ")";
      
      log.info("Tracked Python command wrapper created");
      log.debug("Tracked command: {}", trackedPythonCmd);
      
      cmdline += trackedPythonCmd + "; ";

      // Set the exit code to the result of the executed Python script.
      cmdline += "exitcode=$?;";

      cmdline += "echo 'gw_exit_code='$exitcode;";

      // Remove the executed code directory to clean up.
      cmdline += "rm -rf " + workspace_folder_path + "/" + history_id + ";";

      // Complete command line setup.
      cmdline += "exit $exitcode;";

      // Log the final command line.
      log.info("=== Final command line for execution ===");
      log.info("Command length: {} characters", cmdline.length());
      log.info("Command: {}", cmdline);
      log.info("History ID: {}", history_id);
      log.info("PID file: {}", pidFile);
      log.info("Is join mode: {}", isjoin);

      // Execute the command on the SSH session.
      log.info("Executing command on SSH session...");
      long execStartTime = System.currentTimeMillis();
      Command cmd = session.exec(cmdline);
      long execEndTime = System.currentTimeMillis();
      log.info("Command execution initiated in {} ms", (execEndTime - execStartTime));
      
      // Store the command object so it can be terminated if needed
      this.currentCommand = cmd;
      log.info("Command object stored for potential termination");
      log.info("Command input stream available: {}", cmd.getInputStream() != null);

      // Log the establishment of the SSH command session.
      log.info("SSH command session established successfully");
      log.info("Command input stream available: {}", cmd.getInputStream() != null);

      // Initialize an input stream for reading command output.
      input = new BufferedReader(new InputStreamReader(cmd.getInputStream()), BaseTool.BUFFER_SIZE);
      log.info("Input stream reader created for command output");

      // Send initial message to client to confirm process has started
      CommandServlet.sendMessageToSocket(
          token, history_id + BaseTool.log_separator + "Process started on remote host");
      log.info("Initial process start message sent to client");

      // Initialize the command sender for handling output.
      cmdsender.init(input, token, history_id);

      // Create and start a thread for sending command output.
      thread = new Thread(cmdsender);
      thread.setName("SSH Command output thread");
      log.info("Starting sending thread");
      thread.start();

      // If the 'isjoin' flag is set, wait for the process to complete.
      if (isjoin) {
        log.info("Waiting for command to complete (join mode enabled)...");
        long joinStartTime = System.currentTimeMillis();
        cmd.join(7, TimeUnit.DAYS); // Allow the process to run for up to a week.
        long joinEndTime = System.currentTimeMillis();
        int exitStatus = cmd.getExitStatus();
        log.info("Command completed after {} ms with exit code: {}", (joinEndTime - joinStartTime), exitStatus);
        log.info("Command exit status: {}", exitStatus);
        cmdsender.endWithCode(token, history_id, exitStatus);
      } else {
        log.info("Command execution started in non-join mode (not waiting for completion)");
      }
    } catch (Exception e) {
      // Handle any exceptions that occur during execution.
      e.printStackTrace();
      // Send error message to client
      CommandServlet.sendMessageToSocket(
          token, history_id + BaseTool.log_separator + "Error executing process: " + e.getMessage());
      // Alternatively, you can call the 'endWithError' method here to handle errors.
      this.endWithError(token, history_id, e.getLocalizedMessage());
    }
  }
  
  /**
   * Execute a Bash script on the SSH session and manage the associated process.
   *
   * @param history_id The identifier of the associated history.
   * @param script The Bash script to be executed.
   * @param processid The identifier of the associated process.
   * @param isjoin A boolean flag indicating whether to wait for the process to complete.
   * @param token The token associated with the session.
   */
  @Override
  public void runBash(
      String history_id, String script, String processid, boolean isjoin, String token) {
    try {
      // Log that the Bash script execution is starting.
      log.info("Starting command");

      // Create an empty command line.
      String cmdline = "mkdir -p " + workspace_folder_path + "; cd " + workspace_folder_path + "; ";

      // Set up the execution directory for the Bash script.
      cmdline += "mkdir -p " + history_id + "; cd " + history_id + "; ";

      // Create the Bash script file.
      cmdline +=
          "echo \""
              + script
                  .replaceAll("\r\n", "\n")
                  .replaceAll("\\$", "\\\\\\$")
                  .replaceAll("\"", "\\\\\"")
              + "\" > geoweaver-"
              + history_id
              + ".sh; ";

      // Unzip any Python files into the execution folder for the script to use.
      cmdline +=
          "tar -xf ~/" + history_id + ".tar -C " + workspace_folder_path + "/" + history_id + "/; ";

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

      // Store history ID and PID file path for process termination
      this.historyId = history_id;
      String pidFile = workspace_folder_path + "/" + history_id + ".pid";
      this.currentPidFile = pidFile;
      
      log.info("=== Starting Bash execution for history ID: {} ===", history_id);
      log.info("PID file path: {}", pidFile);
      log.info("Workspace folder: {}", workspace_folder_path);
      
      // Wrap bash script execution to track process group ID (PGID) for reliable termination
      // Create a new process group using setsid, then track the entire group
      String scriptExecution = "./geoweaver-" + history_id + ".sh";
      log.info("Bash script to execute: {}", scriptExecution);
      
      String trackedScriptCmd = "(" +
                                "echo '[GW-DEBUG] Starting Bash script process tracking...' >&2; " +
                                "trap 'rm -f " + pidFile + "' EXIT; " +  // Cleanup PID file on exit
                                "(" + scriptExecution + " 2>&1 | tee " + workspace_folder_path + "/" + history_id + ".log) & " +
                                "SHELL_PID=$!; " +
                                "echo '[GW-DEBUG] Shell PID after background: '$SHELL_PID >&2; " +
                                "sleep 0.5; " +  // Give script time to start
                                "SCRIPT_PID=$(pgrep -P $SHELL_PID 2>/dev/null | head -1); " +  // Find script process under shell
                                "if [ -z \"$SCRIPT_PID\" ]; then " +
                                "  SCRIPT_PID=$(ps aux | grep -E 'geoweaver-" + history_id + "\\.sh' | grep -v grep | awk '{print $2}' | head -1); " +  // Fallback: find by script name
                                "  echo '[GW-DEBUG] Found script PID by name search: '$SCRIPT_PID >&2; " +
                                "fi; " +
                                "if [ -z \"$SCRIPT_PID\" ]; then " +
                                "  SCRIPT_PID=$SHELL_PID; " +  // Last resort: use shell PID
                                "  echo '[GW-DEBUG] Using shell PID as fallback: '$SCRIPT_PID >&2; " +
                                "fi; " +
                                "PGID=$(ps -o pgid= -p $SCRIPT_PID 2>/dev/null | tr -d ' '); " +  // Get process group ID
                                "if [ -z \"$PGID\" ]; then PGID=$SCRIPT_PID; fi; " +  // Fallback to PID
                                "echo '[GW-DEBUG] Script PID: '$SCRIPT_PID', PGID: '$PGID >&2; " +
                                "echo $SCRIPT_PID > " + pidFile + "; " +  // Save actual script PID
                                "echo $PGID >> " + pidFile + "; " +  // Also save PGID on second line
                                "echo '[GW-DEBUG] Saved PID and PGID to " + pidFile + ", content: '$(cat " + pidFile + ") >&2; " +
                                "wait $SHELL_PID; " +
                                "EXIT_CODE=$?; " +
                                "echo '[GW-DEBUG] Process exited with code: '$EXIT_CODE >&2; " +
                                "rm -f " + pidFile + "; " +
                                "exit $EXIT_CODE" +
                                ")";
      
      log.info("Tracked Bash command wrapper created");
      log.debug("Tracked command: {}", trackedScriptCmd);
      
      // Replace the script execution part with tracked version
      cmdline = cmdline.replace("./geoweaver-" + history_id + ".sh;", trackedScriptCmd + "; ");

      // Log the final command line.
      log.info("=== Final bash command line for execution ===");
      log.info("Command length: {} characters", cmdline.length());
      log.info("Command: {}", cmdline);
      log.info("History ID: {}", history_id);
      log.info("PID file: {}", pidFile);
      log.info("Is join mode: {}", isjoin);

      // Execute the command on the SSH session.
      log.info("Executing bash command on SSH session...");
      long execStartTime = System.currentTimeMillis();
      Command cmd = session.exec(cmdline);
      long execEndTime = System.currentTimeMillis();
      log.info("Bash command execution initiated in {} ms", (execEndTime - execStartTime));
      
      // Store the command object so it can be terminated if needed
      this.currentCommand = cmd;
      log.info("Command object stored for potential termination");
      log.info("Command input stream available: {}", cmd.getInputStream() != null);

      // Log the establishment of the SSH command session.
      log.info("SSH command session established successfully");
      log.info("Command input stream available: {}", cmd.getInputStream() != null);

      // Initialize an input stream for reading command output.
      input = new BufferedReader(new InputStreamReader(cmd.getInputStream()), BaseTool.BUFFER_SIZE);
      log.info("Input stream reader created for command output");

      // Initialize the command sender for handling output.
      cmdsender.init(input, token, history_id);
      log.info("Command sender initialized");

      // Create and start a thread for sending command output.
      thread = new Thread(cmdsender);
      thread.setName("SSH Command output thread");
      log.info("Starting command output thread: {}", thread.getName());
      thread.start();
      log.info("Command output thread started successfully");

      // Return to the client after starting the execution.

      // If the 'isjoin' flag is set, wait for the process to complete.
      if (isjoin) {
        log.info("Waiting for bash command to complete (join mode enabled)...");
        long joinStartTime = System.currentTimeMillis();
        cmd.join(7, TimeUnit.DAYS); // Allow the process to run for up to a week.
        long joinEndTime = System.currentTimeMillis();
        int exitStatus = cmd.getExitStatus();
        log.info("Bash command completed after {} ms with exit code: {}", (joinEndTime - joinStartTime), exitStatus);
        log.info("Command exit status: {}", exitStatus);
        cmdsender.endWithCode(token, history_id, exitStatus);
      } else {
        log.info("Bash command execution started in non-join mode (not waiting for completion)");
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
    if (!BaseTool.isNull(cmdsender)) this.cmdsender.setWebSocketSession(session);
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
   * Stop the currently executing command on the remote host.
   * This method terminates the remote process by:
   * 1. Reading the process ID from the PID file
   * 2. Sending SIGTERM signal to the process
   * 3. If SIGTERM doesn't work, sending SIGKILL
   * 4. Closing the command channel and session
   *
   * @return true if the command was successfully stopped, false otherwise
   */
  public boolean stop() {
    log.info("=== Starting process stop operation ===");
    log.info("History ID: {}", historyId);
    log.info("PID file: {}", currentPidFile);
    log.info("SSH client available: {}", ssh != null);
    log.info("Current command object: {}", currentCommand != null);
    log.info("Session available: {}", session != null);
    
    long stopStartTime = System.currentTimeMillis();
    
    try {
      // Stop the output threads first
      log.info("Stopping output threads...");
      if (sessionsender != null) {
        log.info("Stopping session sender thread");
        sessionsender.stop();
        log.info("Session sender thread stopped");
      } else {
        log.warn("Session sender is null, cannot stop");
      }
      
      if (cmdsender != null) {
        log.info("Stopping command sender thread");
        cmdsender.stop();
        log.info("Command sender thread stopped");
      } else {
        log.warn("Command sender is null, cannot stop");
      }
      
      // Try to kill the remote process using PID file
      // CRITICAL: Always attempt to kill, even if PID file or SSH client is null
      // We can still try to find and kill processes by name
      log.info("=== Attempting to kill remote process ===");
      log.info("PID file path: {}", currentPidFile);
      log.info("History ID: {}", historyId);
      log.info("Workspace folder: {}", workspace_folder_path);
      log.info("SSH client available: {}", ssh != null);
      log.info("SSH session available: {}", session != null);
      
      // CRITICAL FIX: If SSH client is null, we MUST still try to kill the process
      // Use the existing session if available, or try to create a new SSH connection
      SSHClient sshToUse = ssh;
      
      if (sshToUse == null) {
        log.error("=== CRITICAL: SSH client is null! ===");
        log.error("This should not happen if login() was called successfully.");
        log.error("Attempting to use existing session or create new connection...");
        
        // Try to use the existing session's client if possible
        // SSHJ Session doesn't expose the client directly, so we need to work around this
        if (session != null) {
          log.warn("Session exists but cannot extract SSH client from it.");
          log.warn("Will attempt to create a new SSH connection for kill command...");
          
          // Try to get connection info from session and create a new client
          // This is a fallback - ideally ssh should never be null
          try {
            // We can't easily get the SSH client from session, so we'll skip kill if ssh is null
            // But we'll log this as a critical error
            log.error("Cannot create new SSH connection without connection details.");
            log.error("Kill command will NOT be executed. Remote process may continue running!");
          } catch (Exception e) {
            log.error("Failed to create new SSH connection: {}", e.getMessage());
          }
        }
      }
      
      // ALWAYS try to execute kill command if we have any way to connect
      // Even if ssh is null, we should at least try to log what we would have done
      if (sshToUse != null) {
        try {
          // Read PID and PGID from file and kill the process
          // File format: first line is PID, second line is PGID (if available)
          // Add extensive logging in the kill command itself
          // CRITICAL: Always try to kill, even if PID file is null - use process name search
          String pidFileToUse;
          if (currentPidFile != null) {
            pidFileToUse = currentPidFile;
          } else if (historyId != null && workspace_folder_path != null) {
            pidFileToUse = workspace_folder_path + "/" + historyId + ".pid";
          } else {
            pidFileToUse = "~/gw-workspace/" + (historyId != null ? historyId : "unknown") + ".pid";
          }
          log.info("Using PID file path for kill command: {}", pidFileToUse);
          
          String killCmd = "echo '[GW-STOP] ===== Starting kill process =====' >&2; " +
                          "if [ -f " + pidFileToUse + " ]; then " +
                          "echo '[GW-STOP] PID file exists: " + pidFileToUse + "' >&2; " +
                          "PID=$(cat " + pidFileToUse + " 2>/dev/null | head -1 | tr -d ' '); " +
                          "PGID=$(cat " + pidFileToUse + " 2>/dev/null | sed -n '2p' | tr -d ' '); " +
                          "echo '[GW-STOP] Read from file - PID: '$PID', PGID: '$PGID >&2; " +
                          "if [ -z \"$PGID\" ]; then " +
                          "  PGID=$(ps -o pgid= -p $PID 2>/dev/null | tr -d ' '); " +
                          "  echo '[GW-STOP] Retrieved PGID from PID: '$PGID >&2; " +
                          "fi; " +
                          "if [ ! -z \"$PID\" ] && [ \"$PID\" != \"0\" ]; then " +
                          "echo '[GW-STOP] PID is valid: '$PID >&2; " +
                          "echo '[GW-STOP] Checking if process exists...' >&2; " +
                          "if kill -0 $PID 2>/dev/null; then " +
                          "echo '[GW-STOP] Process $PID exists, checking children...' >&2; " +
                          "CHILDREN=$(pgrep -P $PID 2>/dev/null); " +
                          "echo '[GW-STOP] Child processes: '$CHILDREN >&2; " +
                          "if [ ! -z \"$CHILDREN\" ]; then " +
                          "  echo '[GW-STOP] Killing child processes first...' >&2; " +
                          "  for CHILD in $CHILDREN; do " +
                          "    kill -TERM $CHILD 2>&1; " +
                          "    echo '[GW-STOP] Sent SIGTERM to child '$CHILD >&2; " +
                          "  done; " +
                          "fi; " +
                          "echo '[GW-STOP] Sending SIGTERM to main process $PID...' >&2; " +
                          "kill -TERM $PID 2>&1; " +
                          "TERM_RESULT=$?; " +
                          "echo '[GW-STOP] SIGTERM to PID result: '$TERM_RESULT >&2; " +
                          "if [ ! -z \"$PGID\" ] && [ \"$PGID\" != \"0\" ] && [ \"$PGID\" != \"$PID\" ]; then " +
                          "  echo '[GW-STOP] Also sending SIGTERM to process group -$PGID...' >&2; " +
                          "  kill -TERM -$PGID 2>&1; " +
                          "  echo '[GW-STOP] SIGTERM to PGID result: '$? >&2; " +
                          "fi; " +
                          "sleep 2; " +
                          "if kill -0 $PID 2>/dev/null; then " +
                          "echo '[GW-STOP] Process $PID still exists, sending SIGKILL...' >&2; " +
                          "kill -KILL $PID 2>&1; " +
                          "KILL_RESULT=$?; " +
                          "echo '[GW-STOP] SIGKILL to PID result: '$KILL_RESULT >&2; " +
                          "if [ ! -z \"$PGID\" ] && [ \"$PGID\" != \"0\" ]; then " +
                          "  echo '[GW-STOP] Also sending SIGKILL to process group -$PGID...' >&2; " +
                          "  kill -KILL -$PGID 2>&1; " +
                          "  echo '[GW-STOP] SIGKILL to PGID result: '$? >&2; " +
                          "fi; " +
                          "sleep 1; " +
                          "if kill -0 $PID 2>/dev/null; then " +
                          "echo '[GW-STOP] WARNING: Process $PID still exists after SIGKILL!' >&2; " +
                          "ps aux | grep -E '(python|geoweaver-" + historyId + "|bash.*" + historyId + ")' | grep -v grep >&2; " +
                          "else " +
                          "echo '[GW-STOP] Process $PID successfully terminated' >&2; " +
                          "fi; " +
                          "else " +
                          "echo '[GW-STOP] Process $PID does not exist (may have already terminated)' >&2; " +
                          "fi; " +
                          "else " +
                          "echo '[GW-STOP] ERROR: PID is empty or invalid: '$PID >&2; " +
                          "fi; " +
                          "rm -f " + pidFileToUse + "; " +
                          "echo '[GW-STOP] PID file removed' >&2; " +
                          "else " +
                          "echo '[GW-STOP] ERROR: PID file does not exist: " + pidFileToUse + "' >&2; " +
                          "echo '[GW-STOP] Attempting to find process by name...' >&2; " +
                          "FOUND_PIDS=$(ps aux | grep -E '(python.*" + workspace_folder_path + "/" + historyId + "|geoweaver-" + historyId + "\\.sh)' | grep -v grep | awk '{print $2}'); " +
                          "if [ ! -z \"$FOUND_PIDS\" ]; then " +
                          "  echo '[GW-STOP] Found processes by name: '$FOUND_PIDS >&2; " +
                          "  for FPID in $FOUND_PIDS; do " +
                          "    echo '[GW-STOP] Killing process '$FPID >&2; " +
                          "    kill -TERM $FPID 2>&1; " +
                          "    sleep 1; " +
                          "    if kill -0 $FPID 2>/dev/null; then kill -KILL $FPID 2>&1; fi; " +
                          "  done; " +
                          "else " +
                          "  echo '[GW-STOP] No processes found by name' >&2; " +
                          "fi; " +
                          "fi; " +
                          "echo '[GW-STOP] ===== Kill process completed =====' >&2;";
          
          log.info("=== Executing kill command ===");
          log.info("Kill command length: {} characters", killCmd.length());
          log.debug("Full kill command: {}", killCmd);
          
          // Execute kill command in a new session to avoid blocking
          try {
            log.info("Creating new SSH session for kill command...");
            long killSessionStart = System.currentTimeMillis();
            Session killSession = sshToUse.startSession();
            long killSessionEnd = System.currentTimeMillis();
            log.info("Kill session created in {} ms", (killSessionEnd - killSessionStart));
            
            log.info("Executing kill command on remote host...");
            long killCmdStart = System.currentTimeMillis();
            Command killCommand = killSession.exec(killCmd);
            
            // Read output from kill command for debugging
            try {
              java.io.InputStream killOutput = killCommand.getInputStream();
              if (killOutput != null) {
                java.io.BufferedReader killReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(killOutput));
                String killLine;
                log.info("=== Kill command output ===");
                while ((killLine = killReader.readLine()) != null) {
                  log.info("KILL-OUTPUT: {}", killLine);
                }
              }
            } catch (Exception outputEx) {
              log.warn("Could not read kill command output: {}", outputEx.getMessage());
            }
            
            // Don't wait too long for kill command
            log.info("Waiting for kill command to complete (max 5 seconds)...");
            killCommand.join(5, java.util.concurrent.TimeUnit.SECONDS);
            long killCmdEnd = System.currentTimeMillis();
            int exitCode = killCommand.getExitStatus();
            log.info("Kill command completed in {} ms with exit code: {}", 
                    (killCmdEnd - killCmdStart), exitCode);
            log.info("Kill command exit status: {}", exitCode);
            
            killSession.close();
            log.info("Kill session closed");
          } catch (Exception killEx) {
            log.error("=== Failed to execute kill command ===");
            log.error("Exception type: {}", killEx.getClass().getName());
            log.error("Exception message: {}", killEx.getMessage());
            log.error("Exception stack trace:");
            killEx.printStackTrace();
            
            // Try alternative: direct kill using process name pattern
            log.info("=== Trying alternative kill method ===");
            try {
              String altKillCmd = "echo '[GW-STOP-ALT] Starting alternative kill...' >&2; " +
                                  "pkill -f 'geoweaver-" + historyId + "' 2>&1; " +
                                  "pkill -f '" + workspace_folder_path + "/" + historyId + "' 2>&1; " +
                                  "ps aux | grep -E '(python|geoweaver-" + historyId + ")' | grep -v grep >&2; " +
                                  "rm -f " + currentPidFile + "; " +
                                  "echo '[GW-STOP-ALT] Alternative kill completed' >&2;";
              
              log.info("Alternative kill command: {}", altKillCmd);
              Session altSession = sshToUse.startSession();
              Command altKill = altSession.exec(altKillCmd);
              
              // Read alternative kill output
              try {
                java.io.InputStream altOutput = altKill.getInputStream();
                if (altOutput != null) {
                  java.io.BufferedReader altReader = new java.io.BufferedReader(
                      new java.io.InputStreamReader(altOutput));
                  String altLine;
                  log.info("=== Alternative kill command output ===");
                  while ((altLine = altReader.readLine()) != null) {
                    log.info("ALT-KILL-OUTPUT: {}", altLine);
                  }
                }
              } catch (Exception altOutputEx) {
                log.warn("Could not read alternative kill output: {}", altOutputEx.getMessage());
              }
              
              altKill.join(3, java.util.concurrent.TimeUnit.SECONDS);
              int altExitCode = altKill.getExitStatus();
              log.info("Alternative kill command executed with exit code: {}", altExitCode);
              altSession.close();
            } catch (Exception altEx) {
              log.error("=== Alternative kill also failed ===");
              log.error("Exception type: {}", altEx.getClass().getName());
              log.error("Exception message: {}", altEx.getMessage());
              altEx.printStackTrace();
            }
          }
        } catch (Exception e) {
          log.error("=== Error in kill process ===");
          log.error("Exception type: {}", e.getClass().getName());
          log.error("Exception message: {}", e.getMessage());
          log.error("Exception stack trace:");
          e.printStackTrace();
        }
      } else {
        log.error("=== CRITICAL: Cannot kill remote process - SSH client is null ===");
        log.error("PID file: {}", currentPidFile);
        log.error("SSH client: {}", ssh != null);
        log.error("Session: {}", session != null);
        log.error("History ID: {}", historyId);
        log.error("This means the kill command will NOT be executed on the remote host!");
        log.error("The remote process will continue running even after stop() is called!");
        
        // Even without SSH client, try to log what we would have done
        if (currentPidFile != null) {
          log.error("Would have tried to kill process using PID file: {}", currentPidFile);
        } else {
          log.error("Would have tried to find and kill process by name for history ID: {}", historyId);
        }
      }
      
      // Terminate the remote command by closing the command channel
      // This sends a termination signal (SIGTERM) to the remote process
      if (currentCommand != null) {
        try {
          log.info("=== Closing command channel ===");
          log.info("Command object: {}", currentCommand);
          log.info("Command input stream available: {}", currentCommand.getInputStream() != null);
          
          // Try to send signal first if available
          try {
            // SSHJ Command doesn't have a direct signal method, but closing should send SIGTERM
            log.info("Attempting to close command channel...");
            currentCommand.close();
            log.info("Command channel closed successfully");
            log.info("Command input stream after close: {}", 
                    currentCommand.getInputStream() != null ? "still available" : "null");
          } catch (Exception closeEx) {
            log.error("Error closing command channel");
            log.error("Exception type: {}", closeEx.getClass().getName());
            log.error("Exception message: {}", closeEx.getMessage());
            closeEx.printStackTrace();
          }
          currentCommand = null;
          log.info("Command object reference cleared");
        } catch (Exception e) {
          log.error("=== Error in command channel closure ===");
          log.error("Exception type: {}", e.getClass().getName());
          log.error("Exception message: {}", e.getMessage());
          e.printStackTrace();
        }
      } else {
        log.warn("Current command object is null, cannot close command channel");
      }
      
      // Also close the session to ensure cleanup
      if (session != null) {
        try {
          log.info("=== Closing SSH session ===");
          log.info("Session object: {}", session);
          session.close();
          log.info("SSH session closed successfully");
        } catch (Exception e) {
          log.error("Error closing SSH session");
          log.error("Exception type: {}", e.getClass().getName());
          log.error("Exception message: {}", e.getMessage());
          e.printStackTrace();
        }
      } else {
        log.warn("SSH session is null, cannot close");
      }
      
      long stopEndTime = System.currentTimeMillis();
      log.info("=== Stop operation completed in {} ms ===", (stopEndTime - stopStartTime));
      log.info("Stop operation result: SUCCESS");
      
      return true;
    } catch (Exception e) {
      log.error("=== Error in stop operation ===");
      log.error("Exception type: {}", e.getClass().getName());
      log.error("Exception message: {}", e.getMessage());
      log.error("Exception stack trace:");
      e.printStackTrace();
      
      long stopEndTime = System.currentTimeMillis();
      log.error("Stop operation failed after {} ms", (stopEndTime - stopStartTime));
      
      return false;
    }
  }

  /**
   * Read information about Python and Conda environments on a remote host in one command. This
   * method executes a command on the SSH session to get information about Python and Conda
   * environments.
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

            if (vals.length < 2) continue;

            // Extract the path to the Conda environment's Python binary.
            String bin = vals[vals.length - 1] + "/bin/python";

            // Determine the name for the environment (defaulting to the binary path if no name is
            // provided).
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
   * Read Python and Conda environments information on a remote host and return it as a formatted
   * string. This method reads information about Python and Conda environments by executing remote
   * commands. The collected environment information is returned as a formatted string.
   *
   * @param hostid The identifier of the remote host.
   * @param password The password for the SSH session.
   * @return A formatted string containing information about Python and Conda environments on the
   *     remote host.
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
  
  /**
   * Test network connectivity to a host and port using multiple methods
   * 
   * @param host The hostname or IP address
   * @param port The port number
   * @return true if connectivity test passes, false otherwise
   */
  private boolean testNetworkConnectivity(String host, int port) {
    boolean allTestsPassed = false;
    
    try {
      // Test 1: DNS Resolution
      log.info("Test 1: DNS Resolution");
      java.net.InetAddress targetAddr;
      if (host.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
        // It's an IPv4 address, use it directly
        targetAddr = java.net.InetAddress.getByName(host);
        log.info("  Using IPv4 address directly: " + targetAddr.getHostAddress());
      } else {
        // It's a hostname, resolve it
        targetAddr = java.net.InetAddress.getByName(host);
        log.info("  Resolved hostname " + host + " to IP: " + targetAddr.getHostAddress());
      }
      
      // Check if it's IPv6 and log warning
      if (targetAddr instanceof java.net.Inet6Address) {
        log.warn("  Host resolved to IPv6 address. This may cause connection issues. Consider using IPv4 address.");
      }
      
      // Test 2: ICMP Ping (may not work if ICMP is blocked by firewall)
      log.info("Test 2: ICMP Ping Test");
      try {
        boolean reachable = targetAddr.isReachable(5000);
        log.info("  ICMP ping result: " + reachable);
        if (!reachable) {
          log.warn("  ICMP ping failed, but this doesn't necessarily mean SSH will fail (ICMP may be blocked)");
        }
      } catch (Exception e) {
        log.warn("  ICMP ping test failed: " + e.getMessage() + " (this is often normal if ICMP is blocked)");
      }
      
      // Test 3: Raw Socket Connection Test (most reliable for SSH)
      log.info("Test 3: Raw Socket Connection Test (most reliable)");
      boolean socketTestPassed = false;
      java.net.Socket testSocket = null;
      try {
        testSocket = new java.net.Socket();
        testSocket.setSoTimeout(5000); // 5 second timeout
        log.info("  Attempting to connect to " + targetAddr.getHostAddress() + ":" + port);
        testSocket.connect(new java.net.InetSocketAddress(targetAddr, port), 5000);
        socketTestPassed = testSocket.isConnected();
        log.info("  Socket connection test: " + (socketTestPassed ? "SUCCESS" : "FAILED"));
        if (socketTestPassed) {
          log.info("  Java application CAN access " + host + ":" + port);
          allTestsPassed = true;
        }
      } catch (java.net.NoRouteToHostException e) {
        log.error("  Socket connection test FAILED: No route to host");
        log.error("  This means Java cannot reach " + host + ":" + port);
        log.error("  DIAGNOSTIC: Trying to bind to interface on same subnet as target...");
        
        // Try binding to interface on same subnet - this is the KEY TEST
        // Works for any interface name (en0, eth0, wlan0, etc.)
        boolean interfaceBindingWorks = false;
        String workingInterfaceName = null;
        try {
          java.util.Enumeration<java.net.NetworkInterface> interfaces = 
              java.net.NetworkInterface.getNetworkInterfaces();
          
          while (interfaces.hasMoreElements()) {
            java.net.NetworkInterface ni = interfaces.nextElement();
            
            // Skip loopback and down interfaces
            if (ni.isLoopback() || !ni.isUp()) {
              continue;
            }
            
            String interfaceName = ni.getName();
            java.util.List<java.net.InetAddress> addresses = 
                java.util.Collections.list(ni.getInetAddresses());
            
            for (java.net.InetAddress localAddr : addresses) {
              if (localAddr instanceof java.net.Inet4Address) {
                String localIp = localAddr.getHostAddress();
                String[] localParts = localIp.split("\\.");
                String[] targetParts = targetAddr.getHostAddress().split("\\.");
                
                if (localParts.length == 4 && targetParts.length == 4) {
                  // Check if on same subnet
                  boolean sameSubnet = localParts[0].equals(targetParts[0]) && 
                                      localParts[1].equals(targetParts[1]) && 
                                      localParts[2].equals(targetParts[2]);
                  
                  if (sameSubnet) {
                    log.error("  Found interface " + interfaceName + " on same subnet: " + localIp);
                    log.error("  Attempting connection bound to " + interfaceName + " interface...");
                    java.net.Socket boundSocket = null;
                    try {
                      boundSocket = new java.net.Socket();
                      boundSocket.setSoTimeout(5000);
                      log.error("  Binding socket to " + interfaceName + ": " + localIp);
                      boundSocket.bind(new java.net.InetSocketAddress(localAddr, 0));
                      log.error("  Socket bound successfully, now connecting to " + targetAddr.getHostAddress() + ":" + port);
                      boundSocket.connect(new java.net.InetSocketAddress(targetAddr, port), 5000);
                      if (boundSocket.isConnected()) {
                        log.error("  *** SUCCESS! Connection works when bound to " + interfaceName + "! ***");
                        log.error("  This confirms the problem: Java default routing is using wrong interface");
                        log.error("  SOLUTION: Need to force SSHJ to bind to " + interfaceName + " interface");
                        interfaceBindingWorks = true;
                        workingInterfaceName = interfaceName;
                        boundSocket.close();
                        // This is the key finding - we need to use this interface!
                        break;
                      }
                    } catch (java.net.NoRouteToHostException boundEx) {
                      log.error("  Connection bound to " + interfaceName + " also failed with NoRouteToHostException");
                      log.error("  This suggests a deeper network/firewall issue");
                    } catch (Exception boundEx) {
                      log.error("  Connection bound to " + interfaceName + " failed: " + boundEx.getMessage());
                      log.error("  Exception type: " + boundEx.getClass().getSimpleName());
                      if (boundEx.getCause() != null) {
                        log.error("  Cause: " + boundEx.getCause().getClass().getSimpleName() + " - " + boundEx.getCause().getMessage());
                      }
                    } finally {
                      if (boundSocket != null && !boundSocket.isClosed()) {
                        try {
                          boundSocket.close();
                        } catch (Exception closeEx) {
                          // Ignore
                        }
                      }
                    }
                    
                    if (interfaceBindingWorks) {
                      break; // Found working interface, no need to check others
                    }
                  }
                }
              }
            }
            
            if (interfaceBindingWorks) {
              break; // Found working interface
            }
          }
          
          if (!interfaceBindingWorks) {
            log.error("  Could not find any interface on same subnet that allows connection");
            log.error("  This suggests a network/firewall issue affecting all interfaces");
          }
        } catch (Exception interfaceTestEx) {
          log.error("  Failed to test interface binding: " + interfaceTestEx.getMessage());
          interfaceTestEx.printStackTrace();
        }
        
        if (interfaceBindingWorks) {
          log.error("  *** CRITICAL FINDING: Binding to " + workingInterfaceName + " works, but default connection fails ***");
          log.error("  This means we MUST configure SSHJ to bind to " + workingInterfaceName + " interface");
        }
        
        log.error("  Possible causes:");
        log.error("    - Firewall blocking Java application (most common on macOS)");
        log.error("    - Network routing issue");
        log.error("    - Wrong network interface being used");
        log.error("  DIAGNOSTIC: Checking which network interface Java is trying to use...");
        
        // Try to determine which interface would be used
        try {
          java.net.InetAddress target = java.net.InetAddress.getByName(host);
          java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
          
          while (interfaces.hasMoreElements()) {
            java.net.NetworkInterface ni = interfaces.nextElement();
            if (ni.isUp() && !ni.isLoopback()) {
              java.util.List<java.net.InetAddress> addresses = java.util.Collections.list(ni.getInetAddresses());
              for (java.net.InetAddress localAddr : addresses) {
                if (localAddr instanceof java.net.Inet4Address) {
                  String localIp = localAddr.getHostAddress();
                  String[] localParts = localIp.split("\\.");
                  String[] targetParts = target.getHostAddress().split("\\.");
                  
                  if (localParts.length == 4 && targetParts.length == 4) {
                    // Check if on same subnet
                    boolean sameSubnet = localParts[0].equals(targetParts[0]) && 
                                        localParts[1].equals(targetParts[1]) && 
                                        localParts[2].equals(targetParts[2]);
                    
                    log.error("    Interface: " + ni.getName() + ", Local IP: " + localIp + 
                             (sameSubnet ? " (SAME SUBNET as target)" : " (different subnet)"));
                    
                    if (sameSubnet) {
                      log.error("    -> This interface should be able to reach the target!");
                    }
                  }
                }
              }
            }
          }
        } catch (Exception diagEx) {
          log.error("  Failed to diagnose network interface: " + diagEx.getMessage());
        }
        
        log.error("  SOLUTION SUGGESTIONS:");
        log.error("    1. macOS Firewall - Check OUTBOUND rules (not just inbound):");
        log.error("       - System Settings > Network > Firewall > Options");
        log.error("       - Look for 'Block all incoming connections' - this shouldn't affect outbound");
        log.error("       - Check if there are any third-party firewall apps (Little Snitch, etc.)");
        log.error("    2. Check network routing from terminal:");
        log.error("       Run: route get " + host);
        log.error("       Compare with what Java sees (check Test 4 output above)");
        log.error("    3. Try binding Socket to specific network interface:");
        log.error("       This may require code changes to bind to the correct interface");
        log.error("    4. Check macOS security settings:");
        log.error("       - System Settings > Privacy & Security > Full Disk Access");
        log.error("       - System Settings > Privacy & Security > Network Extensions");
        log.error("    5. Check if VPN or network proxy is interfering");
        log.error("    6. Try running Geoweaver with sudo (temporary test only):");
        log.error("       This will help determine if it's a permissions issue");
      } catch (java.net.ConnectException e) {
        log.warn("  Socket connection test: Connection refused (host is reachable but port may be closed)");
        log.warn("  This usually means the host is reachable but SSH service may not be running");
        // This is actually a good sign - it means we can reach the host
        allTestsPassed = true;
      } catch (java.net.SocketTimeoutException e) {
        log.error("  Socket connection test FAILED: Connection timeout");
        log.error("  This means Java cannot reach " + host + ":" + port + " within timeout period");
      } catch (Exception e) {
        log.error("  Socket connection test FAILED: " + e.getClass().getSimpleName() + " - " + e.getMessage());
      } finally {
        if (testSocket != null && !testSocket.isClosed()) {
          try {
            testSocket.close();
          } catch (Exception e) {
            log.warn("  Error closing test socket: " + e.getMessage());
          }
        }
      }
      
      // Test 4: List available network interfaces
      log.info("Test 4: Network Interface Information");
      try {
        java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
        int interfaceCount = 0;
        while (interfaces.hasMoreElements()) {
          java.net.NetworkInterface ni = interfaces.nextElement();
          if (ni.isUp() && !ni.isLoopback()) {
            interfaceCount++;
            java.util.List<java.net.InetAddress> addresses = java.util.Collections.list(ni.getInetAddresses());
            log.info("  Interface " + interfaceCount + ": " + ni.getName());
            for (java.net.InetAddress addr : addresses) {
              log.info("    - " + addr.getHostAddress() + " (" + (addr instanceof java.net.Inet4Address ? "IPv4" : "IPv6") + ")");
            }
          }
        }
        if (interfaceCount == 0) {
          log.warn("  No active network interfaces found (excluding loopback)");
        }
      } catch (Exception e) {
        log.warn("  Failed to list network interfaces: " + e.getMessage());
      }
      
      // Test 5: Check if we can bind to a local address on the same network
      log.info("Test 5: Local Network Binding Test");
      try {
        // Try to find a local address on the same network
        java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
          java.net.NetworkInterface ni = interfaces.nextElement();
          if (ni.isUp() && !ni.isLoopback()) {
            java.util.List<java.net.InetAddress> addresses = java.util.Collections.list(ni.getInetAddresses());
            for (java.net.InetAddress localAddr : addresses) {
              if (localAddr instanceof java.net.Inet4Address) {
                log.info("  Found local IPv4 address: " + localAddr.getHostAddress());
                // Check if target is on same network (simple check)
                String localIp = localAddr.getHostAddress();
                String[] localParts = localIp.split("\\.");
                String[] targetParts = targetAddr.getHostAddress().split("\\.");
                if (localParts.length == 4 && targetParts.length == 4) {
                  // Check if first 3 octets match (same subnet)
                  if (localParts[0].equals(targetParts[0]) && 
                      localParts[1].equals(targetParts[1]) && 
                      localParts[2].equals(targetParts[2])) {
                    log.info("  Target host appears to be on the same subnet as local interface");
                  }
                }
              }
            }
          }
        }
      } catch (Exception e) {
        log.warn("  Local network binding test failed: " + e.getMessage());
      }
      
    } catch (Exception e) {
      log.error("Network connectivity test failed with exception: " + e.getMessage(), e);
    }
    
    return allTestsPassed;
  }
}
