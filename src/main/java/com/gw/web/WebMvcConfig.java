package com.gw.web;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the login interceptor, and keeps a database session open for the whole request
 * (open-session-in-view) on the browser pages only.
 *
 * <p>Agent API requests ({@code /api/v1/**}) are left out: a request that waits up to a minute for
 * a run to finish would otherwise hold a pooled database connection the whole time, so a few
 * waiting agents could use up the pool and block every other request (and the database
 * maintenance that needs a moment without open connections). {@code spring.jpa.open-in-view} is
 * turned off so that this registration replaces Spring Boot's global one.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Autowired private LoginInterceptor loginInterceptor;

  @Autowired private ObjectProvider<EntityManagerFactory> entityManagerFactory;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    EntityManagerFactory emf = entityManagerFactory.getIfAvailable();
    if (emf != null) {
      OpenEntityManagerInViewInterceptor osiv = new OpenEntityManagerInViewInterceptor();
      osiv.setEntityManagerFactory(emf);
      registry.addWebRequestInterceptor(osiv).addPathPatterns("/**").excludePathPatterns("/api/v1/**");
    }
  }
}
