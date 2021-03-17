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

public class SessionPair{
        
        String id;
        
        Session browse_geoweaver_session;
        
        Java2JupyterClientEndpoint geoweaver_jupyter_client;
        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
        
        public Session findOpenSession() {
            
            Set<Session> sessionset = browse_geoweaver_session.getOpenSessions();
            
            Iterator it = sessionset.iterator();
            
            Session session = browse_geoweaver_session;
            
                while(it.hasNext()){
//		        System.out.println(it.next());
                    
                    Session cs = (Session)it.next();
                    
                    if(id.equals(cs.getQueryString())) {
                        
                        session = cs;
                        
                        break;
                        
                    }
                    
                }
                
                return session;
            
        }

        public Session getBrowse_geoweaver_session() {
            
            return findOpenSession();
        }

        public void setBrowse_geoweaver_session(Session browse_geoweaver_session) {
            
            this.browse_geoweaver_session = browse_geoweaver_session;
            
        }

        public Java2JupyterClientEndpoint getGeoweaver_jupyter_client() {
            return geoweaver_jupyter_client;
        }

        public void setGeoweaver_jupyter_client(Java2JupyterClientEndpoint geoweaver_jupyter_client) {
            this.geoweaver_jupyter_client = geoweaver_jupyter_client;
        }
        
    }