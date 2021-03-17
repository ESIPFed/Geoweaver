package com.gw.tools;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gw.jpa.Host;
import com.gw.server.Java2JupyterClientEndpoint;
import com.gw.tools.HostTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;


public class JupyterSessionPairTool {
    
	static Logger logger = LoggerFactory.getLogger(JupyterSessionPairTool.class);
	
//	Session wsSession;
    
//	@Autowired
//	Java2JupyterClientEndpoint client;
    
    public static List<SessionPair> pairs = new ArrayList();
    
    
    /**
     * First session is between browser and geoweaver
     * @param b2gsession
     * @return
     */
    public static SessionPair findPairBy1stSession(Session b2gsession) {
        
        SessionPair pair = null;
        
        for(SessionPair p : pairs) {
            
            if(p.getBrowse_geoweaver_session()==b2gsession) {
                
                pair = p;
                
                break;
                
            }
            
        }
        
        return pair;
        
    }
    
    public static SessionPair findPairByID(String pairid) {
        
        SessionPair pair = null;
        
        for(SessionPair p : pairs) {
            
            if(p.getId().equals(pairid)) {
                
                pair = p;
                
                break;
                
            }
            
        }
        
        return pair;
        
    }
    
    /**
     * 2nd session is between geoweaver and jupyter
     * @param b2gsession
     * @return
     */
    public static SessionPair findPairBy2ndSession(Java2JupyterClientEndpoint g2jclient) {
        
        SessionPair pair = null;
        
        for(SessionPair p : pairs) {
            
            if(p.getGeoweaver_jupyter_client()==g2jclient) {
                
                pair = p;
                
                break;
                
            }
            
        }
        
        return pair;
        
    }
    
    public static void removeClosedPair() {
        
        for(SessionPair p : pairs) {
            
            if(!p.getBrowse_geoweaver_session().isOpen() ) {
                
                System.out.println("Detected one browser_geoweaver_session is closed. Removing it...");
                
                pairs.remove(p);
                
            }
            
            if(!p.getGeoweaver_jupyter_client().getNew_ws_session_between_geoweaver_and_jupyterserver().isOpen()) {
                
                System.out.println("Detected one geoweaver jupyter session is closed. Removing it...");
                
                pairs.remove(p);
                
            }
            
        }
        
    }
    
    public static void removePairByID(String id) {
        
        for(SessionPair p : pairs) {
            
            if(p.getId().equals(id)) {
                
                System.out.println("Detected one session pair is closed on one of the websocket session. Removing it...");
                
                pairs.remove(p);
                
                break;
                
            }
            
        }
        
    }
    
    
    
}
