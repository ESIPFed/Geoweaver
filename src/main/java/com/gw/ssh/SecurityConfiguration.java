package com.gw.ssh;

import java.util.Arrays;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  protected final Logger log = Logger.getLogger(getClass());

  private final SSHAuthenticationProvider sshAuthenticationProvider;

  public SecurityConfiguration(SSHAuthenticationProvider sshAuthenticationProvider) {
    this.sshAuthenticationProvider = sshAuthenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    log.info("registering SSH authentication provider");
    AuthenticationManagerBuilder authenticationManagerBuilder =
        http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder.authenticationProvider(sshAuthenticationProvider);
    return authenticationManagerBuilder.build();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth -> auth.requestMatchers("/Geoweaver/**").permitAll().anyRequest().permitAll())
        .formLogin(form -> form.loginProcessingUrl("/Geoweaver/users/login"))
        .logout(logout -> {})
        .cors(cors -> {})
        .csrf(AbstractHttpConfigurer::disable)
        .headers(HeadersConfigurer::disable);

    return http.build();
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
