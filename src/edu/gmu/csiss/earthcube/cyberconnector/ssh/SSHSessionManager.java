package edu.gmu.csiss.earthcube.cyberconnector.ssh;
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

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHSessionManager {

    protected final Logger          log = LoggerFactory.getLogger(getClass());
    
    // TODO consider alternative implementation
    public final ConcurrentHashMap<String, SSHSession> sessionsByWebsocketID = new ConcurrentHashMap<String, SSHSession>();   
    
    public final ConcurrentHashMap<String, SSHSession> sessionsByUsername = new ConcurrentHashMap<String, SSHSession>();   
    
    public final ConcurrentHashMap<String, SSHSession> sessionsByToken = new ConcurrentHashMap<String, SSHSession>();
    
    public void closeAll() {
    	
    	try {
    		
    		for (Entry<String, SSHSession> o : sessionsByUsername.entrySet()) {
        	    
        		o.getValue().logout();
        		
        	}
    		
        	for (Entry<String, SSHSession> o : sessionsByWebsocketID.entrySet()) {
        	    
        		o.getValue().logout();
        		
        	}
        	
        	for (Entry<String, SSHSession> o : sessionsByToken.entrySet()) {
        	    
        		o.getValue().logout();
        		
        	}
        	
        	sessionsByUsername.clear();
        	
        	sessionsByWebsocketID.clear();
        	
        	sessionsByToken.clear();
        	
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    }
    
}
