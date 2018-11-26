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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


public class SSHAuthenticationProvider implements AuthenticationProvider {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
    
    //@Autowired
    public SSHSessionManager sshSessionManager = new SSHSessionManager();

    public SSHAuthenticationProvider() {
        authorities.add(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SSHSession sshSession = new SSHSessionImpl();
        //UsernamePasswordAuthenticationToken request = (UsernamePasswordAuthenticationToken) authentication;
        String username = authentication.getPrincipal().toString();
        String password = authentication.getCredentials().toString();
        //log.info("{}:{}", username, password);
        boolean success = sshSession.login("", "", username, password, "", false);
        log.info("SSH login: {}={}", username, success);
        Authentication result = new UsernamePasswordAuthenticationToken(username, password, authorities);
        //result.setAuthenticated(success);
        log.info("adding SSH session for {}", username);
//        sshSessionManager.sessionsByUsername.put(username, sshSession);
        return result;
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
