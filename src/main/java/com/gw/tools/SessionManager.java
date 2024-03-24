package com.gw.tools;

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

import com.gw.local.LocalSession;
import com.gw.ssh.SSHSession;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Manage all the SSH connection sessions between Geoweaver and local/remote servers */
@Service
public class SessionManager {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  // TODO consider alternative implementation
  /** this map onlys contains the ssh sessions with one or more associated websocket sessions */
  //    public final ConcurrentHashMap<String, SSHSession> sessionsByWebsocketID = new
  // ConcurrentHashMap<String, SSHSession>();

  //    public final ConcurrentHashMap<String, SSHSession> sessionsByUsername = new
  // ConcurrentHashMap<String, SSHSession>();
  /** this map contains all the ssh sessions Key: HttpSession ID Value: Open SSH Terminal Session */
  public final ConcurrentHashMap<String, SSHSession> sshSessionByToken =
      new ConcurrentHashMap<String, SSHSession>();

  public final ConcurrentHashMap<String, LocalSession> localSessionByToken =
      new ConcurrentHashMap<String, LocalSession>();

  /**
   * This function should clean those sessions which have been stopped but still exist in the
   * manager
   */
  public void cleanClosed() {

    // clean the ssh session

    Iterator it = sshSessionByToken.entrySet().iterator();

    while (it.hasNext()) {

      ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();

      SSHSession ssh = (SSHSession) pair.getValue();

      String key = (String) pair.getKey();

      if (!ssh.getSSHJSession().isOpen()) sshSessionByToken.remove(key);
    }

    // clean the local session

    it = localSessionByToken.entrySet().iterator();

    while (it.hasNext()) {

      ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();

      LocalSession localsession = (LocalSession) pair.getValue();

      String key = (String) pair.getKey();

      if (localsession.isClose()) sshSessionByToken.remove(key);
    }
  }

  public void closeByToken(String token) {

    if (sshSessionByToken.containsKey(token)) {

      log.info("log out the ssh session");

      SSHSession sshSession = sshSessionByToken.get(token);

      sshSession.logout();

      sshSessionByToken.remove(token);
    }

    if (localSessionByToken.containsKey(token)) {

      log.info("log out the local session");

      LocalSession localSession = localSessionByToken.get(token);

      if (localSession != null) {
        localSession.stop();

        localSessionByToken.remove(token);
      }
    }
  }

  public void closeWebSocketByToken(String token) {

    // close all the related websocket sessions
    // updated: websocket will be automatically closed if it detects no SSH session on the back end

    //		Iterator it = sessionsByWebsocketID.entrySet().iterator();
    //
    //		while (it.hasNext()) {
    //
    //	    	ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry)it.next();
    //
    //	    	SSHSession ssh = (SSHSession)pair.getValue();
    //
    //	    	if(ssh.getToken().equals(token)) {
    //
    //	    		String websocketid = (String)pair.getKey();
    //
    //	    		try {
    //
    //	    			ShellSocket.findSessionById(websocketid).close();
    //
    //	    			sessionsByWebsocketID.remove(websocketid);
    //
    //				} catch (Exception e) {
    //
    //					e.printStackTrace();
    //
    //				}
    //
    //	    	}
    //	    }

  }

  /** Close all SSH sessions */
  public void closeAll() {

    try {

      //    		for (Entry<String, SSHSession> o : sessionsByUsername.entrySet()) {
      //
      //        		o.getValue().logout();
      //
      //        	}

      //        	for (Entry<String, SSHSession> o : sessionsByWebsocketID.entrySet()) {
      //
      //        		String websocketid = (String)o.getKey();
      //
      //        		ShellSocket.findSessionById(websocketid).close();
      //
      //        		o.getValue().logout();
      //
      //        	}

      for (Entry<String, SSHSession> o : sshSessionByToken.entrySet()) {

        o.getValue().logout();
      }

      for (Entry<String, LocalSession> o : localSessionByToken.entrySet()) {

        o.getValue().stop();
      }

      //        	sessionsByUsername.clear();

      //        	sessionsByWebsocketID.clear();

      sshSessionByToken.clear();

      localSessionByToken.clear();

    } catch (Exception e) {

      e.printStackTrace();
    }
  }
}
