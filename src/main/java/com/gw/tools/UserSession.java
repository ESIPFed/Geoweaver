package com.gw.tools;

import java.util.Date;

public class UserSession {

    String userid;

    Date created_time;

    String ip_address;

    String jssessionid;

    public String getJssessionid() {
        return this.jssessionid;
    }

    public void setJssessionid(String jssessionid) {
        this.jssessionid = jssessionid;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Date getCreated_time() {
        return this.created_time;
    }

    public void setCreated_time(Date created_time) {
        this.created_time = created_time;
    }

    public String getIp_address() {
        return this.ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }


    
}
