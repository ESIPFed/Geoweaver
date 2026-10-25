package com.gw.ssh;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
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

  /**
   * Comma-separated origins (e.g. {@code https://portal.example.org}) whose pages may call the GUI
   * endpoints with the user's session cookie. Empty (default) means same-origin only.
   */
  @Value("${geoweaver.cors.allowed-origins:}")
  private String allowedOrigins;

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    // Agent API and its schema: Bearer-header auth, so any origin may call it but never with
    // cookies. Registered first so it wins over the GUI rule below.
    CorsConfiguration api = new CorsConfiguration();
    api.setAllowedOriginPatterns(List.of("*"));
    api.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    api.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Geoweaver-Agent-Token"));
    api.setAllowCredentials(false);
    api.setMaxAge(3600L);
    source.registerCorsConfiguration("/api/v1/**", api);
    source.registerCorsConfiguration("/v3/api-docs/**", api);

    List<String> origins =
        Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(o -> !o.isEmpty())
            .collect(Collectors.toList());
    if (!origins.isEmpty()) {
      CorsConfiguration gui = new CorsConfiguration();
      gui.setAllowedOrigins(origins);
      gui.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      gui.setAllowedHeaders(List.of("*"));
      gui.setAllowCredentials(true);
      source.registerCorsConfiguration("/**", gui);
    }
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
