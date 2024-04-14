package com.gw.tools;

import java.util.Iterator;
import java.util.Set;
import javax.websocket.Session;

public class SessionPair {

  String id;

  Session browse_geoweaver_session;

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


}
