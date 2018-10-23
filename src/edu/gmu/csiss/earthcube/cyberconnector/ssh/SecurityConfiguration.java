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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private SSHSessionManager sshSessionManager;

	@Bean
	public SSHAuthenticationProvider sshAuthentication() {
		SSHAuthenticationProvider sshAuthentication = new SSHAuthenticationProvider();
		return sshAuthentication;
	}

	/*
	 * TODO - use 'registerAuthentication' in 3.2.0.RC1 and 'configure' in 3.2.0.RELEASE
	 * but note that 'configure' does not appear to work in tomcat7
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		log.info("registering SSH authentication provider");
		auth.authenticationProvider(sshAuthentication());
		// auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
		// auth.apply(new
		// SSHUserDetailsManagerConfigurer<AuthenticationManagerBuilder>());
	}
	
	@Override
	public void configure(WebSecurity builder) throws Exception {
		// builder.ignoring().antMatchers("/ssh/**").antMatchers("/static/**");
	}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/CyberConnect/web/ssh/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/CyberConnector/web/ssh-login")
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/CyberConnector/web/ssh-logout")
                .permitAll();
    }
}
