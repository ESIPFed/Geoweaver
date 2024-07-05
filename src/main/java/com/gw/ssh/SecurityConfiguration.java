package com.gw.ssh;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import com.gw.tools.SessionManager;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  protected final Logger log = Logger.getLogger(getClass());

  @Bean
  public SSHAuthenticationProvider sshAuthentication() {
    SSHAuthenticationProvider sshAuthentication = new SSHAuthenticationProvider();
    return sshAuthentication;
  }

  /*
   * TODO - use 'registerAuthentication' in 3.2.0.RC1 and 'configure' in 3.2.0.RELEASE
   * but note that 'configure' does not appear to work in tomcat7
   */
  /*~~(Migrate manually based on https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)~~>*/@Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    log.info("registering SSH authentication provider");
    auth.authenticationProvider(sshAuthentication());
  }

  /**
   * WebSecurity is used to ask Spring Security to bypass the following resources WebSecurity is
   * based on HttpSecurity
   */
  @Override
  public void configure(WebSecurity builder) throws Exception {}

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.authorizeRequests(requests -> requests
        .formLogin(login -> login
            .loginProcessingUrl("/Geoweaver/users/login"))
        .logout(withDefaults()));

    http.cors(cors -> cors.csrf(csrf -> csrf.disable()));

    http.headers(withDefaults());

    http.headers(withDefaults()); // this must be turned off to make the JupyterHub work
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("*"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
