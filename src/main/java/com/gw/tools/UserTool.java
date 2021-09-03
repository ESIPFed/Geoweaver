package com.gw.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import com.gw.database.UserRepository;
import com.gw.jpa.GWUser;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserTool {
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    BaseTool bt;

    @Autowired
    ProcessTool pt;

    @Autowired
    HostTool ht;

    @Autowired
    WorkflowTool wt;

    
    private final String LOCALHOST_IPV4 = "127.0.0.1";
	private final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    List<UserSession> authsession2user = new ArrayList();

    public Map<String, String> token2userid = new HashMap();

    public Map<String, Date> token2date = new HashMap();

    long TIMEOUT_THRESHOLD = 24*60*60*1000;

    Logger logger = Logger.getLogger(this.getClass());

    public void removeSessionById(String jssessionid){

        for(UserSession u : this.authsession2user){

            if(u.getJssessionid().equals(jssessionid)){

                this.authsession2user.remove(u);

                break;

            }

        }

    }

    public UserSession getBySessionId(String jssessionid){

        UserSession us = null;

        for(UserSession u : this.authsession2user){

            if(u.getJssessionid().equals(jssessionid)){

                us = u;
                break;

            }

        }

        return us;

    }

    public void cleanExpiredAuth(){

        for(UserSession u : this.authsession2user){

            long difference_In_Time
            = new Date().getTime() - u.getCreated_time().getTime();

            if(difference_In_Time>TIMEOUT_THRESHOLD){

                logger.debug("Found Session Expired, removing..");

                this.authsession2user.remove(u);

            }

        }

    }

    public boolean isAuth(String jssessionid, String ipaddress){

        boolean isauth = false;

        UserSession us = getBySessionId(jssessionid);

        String userid = "111111";

        if(!bt.isNull(us)){

            if(us.getIp_address().equals(ipaddress)){

                userid = us.getUserid();

                isauth = true;

            }else{

                logger.debug("Attempts from different location. Need login again. ");
            
            }


        }

        return isauth;

    }

    
    public String getClientIp(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-Forwarded-For");
		if(bt.isNull(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		
		if(bt.isNull(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		
		if(bt.isNull(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if(LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
				try {
					InetAddress inetAddress = InetAddress.getLocalHost();
					ipAddress = inetAddress.getHostAddress();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(!bt.isNull(ipAddress) 
				&& ipAddress.length() > 15
				&& ipAddress.indexOf(",") > 0) {
			ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
		}
		
		return ipAddress;
	}

    public String getAuthUserId(String jssessionid, String ipaddress){

        String userid = "111111";

        if(this.isAuth(jssessionid, ipaddress)){

            UserSession us = getBySessionId(jssessionid);


            if(!bt.isNull(us)){


                userid = us.getUserid();

            }

        }

        return userid;

    }

    public void bindSessionUser(String jssessionid, String userid, String ip_address){

        UserSession us = new UserSession();

        us.setCreated_time(new Date());

        us.setIp_address(ip_address);

        us.setUserid(userid);

        us.setJssessionid(jssessionid);

        authsession2user.add(us);

    }

    public void updatePassword(GWUser user, String password){

        String newpassword = bt.get_SHA_512_SecurePassword(password, user.getId());

        user.setPassword(newpassword);

        this.save(user);


    }

    public GWUser getUserByToken(String token){

        return null;

    }

    public GWUser getUserById(String id){

        GWUser u = null;

        Optional<GWUser> og = userRepository.findById(id);

        if(!og.isEmpty()){

            u = og.get();

        }

        return u;

    }

    public void save(GWUser user){

        userRepository.save(user);

    }

    public void belongToPublicUser(){

        logger.debug("Belong the no-owner resources to public user..");

        pt.getAllProcesses().forEach(p->{

            if(bt.isNull(p.getOwner())){ 

                p.setOwner("111111");

                p.setConfidential("FALSE");

                pt.save(p);

            }else if(bt.isNull(p.getConfidential())){

                p.setConfidential("FALSE");

                pt.save(p);

            }

        });

        ht.getAllHosts().forEach(h->{

            if(bt.isNull(h.getOwner())){

                h.setOwner("111111");

                h.setConfidential("FALSE");

                ht.save(h);

            }else if(bt.isNull(h.getConfidential())){

                h.setConfidential("FALSE");

                ht.save(h);

            }

        });;

        wt.getAllWorkflow().forEach(w->{

            if(bt.isNull(w.getOwner())){

                w.setOwner("111111");

                w.setConfidential("FALSE");

                wt.save(w);

            }

        });


    }

}
