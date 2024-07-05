package com.gw.tools;

import com.gw.server.Java2JupyterClientEndpoint;
import java.util.Iterator;
import java.util.Set;
import jakarta.websocket.Session;

public class SessionPair {

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

    while (it.hasNext()) {
      //		        System.out.println(it.next());

      Session cs = (Session) it.next();

      if (id.equals(cs.getQueryString())) {

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
