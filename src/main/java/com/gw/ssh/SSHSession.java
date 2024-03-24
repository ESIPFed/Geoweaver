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
import java.io.OutputStream;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.socket.WebSocketSession;

public interface SSHSession {

  /**
   * login with user name and password
   *
   * @param host
   * @param port
   * @param username
   * @param password
   * @param token token is websocket token
   * @return
   * @throws AuthenticationException
   */
  public boolean login(
      String host, String port, String username, String password, String token, boolean isTerminal)
      throws AuthenticationException;

  public boolean login(String hostid, String password, String token, boolean isTerminal);

  public boolean logout();

  public Session getSSHJSession();

  public SSHClient getSsh();

  public boolean isTerminal();

  public void setSSHJSession(Session session);

  public String getHistory_process();

  public void setHistory_process(String history_process);

  public String getHistory_id();

  public String readPythonEnvironment(String hostid, String password);

  public BufferedReader getSSHInput();

  public OutputStream getSSHOutput();

  public void setWebSocketSession(WebSocketSession session);

  public void runBash(
      String history_id, String script, String processid, boolean isjoin, String token);

  public void runJupyter(
      String history_id,
      String script,
      String processid,
      boolean isjoin,
      String bin,
      String env,
      String basedir,
      String token);

  public void runPython(
      String history_id,
      String script,
      String processid,
      boolean isjoin,
      String bin,
      String pyenv,
      String basedir,
      String token);

  public void runMultipleBashes(String history_id, String[] script, String processid);

  public void saveHistory(String logs, String status);

  public String getUsername();

  public String getToken();

  public String getHost();

  public String getPort();
}
