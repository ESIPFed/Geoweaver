package com.gw.utils;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * BeanTool is a utility class designed to interact with Spring Application Context for bean
 * management. It provides methods to access beans and the application context within a Spring
 * application.
 */
@Component
public class BeanTool implements ApplicationContextAware {

  // The application context is set by Spring when this class is ApplicationContextAware.
  private static ApplicationContext context;

  /**
   * Called by Spring to set the application context for this class.
   *
   * @param applicationContext The Spring application context.
   * @throws BeansException If an error occurs while setting the application context.
   */
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }

  /**
   * Get the current Spring Application Context.
   *
   * @return The Spring Application Context.
   */
  public static ApplicationContext getApplicationContext() {
    return context;
  }

  /**
   * Get a bean of the specified class from the Spring Application Context.
   * In CLI mode, this method handles the case when certain beans are not available.
   *
   * @param <T> The type of the bean to retrieve.
   * @param beanClass The class of the bean to retrieve.
   * @return An instance of the specified bean class, or null if in CLI mode and the bean is not available.
   */
  public static <T extends Object> T getBean(Class<T> beanClass) {
    if (context == null) {
      Logger.getLogger(BeanTool.class).warn("Application context is null, likely running in CLI mode. Cannot get bean: " + beanClass.getName());
      return null;
    }
    
    try {
      return context.getBean(beanClass);
    } catch (BeansException e) {
      Logger.getLogger(BeanTool.class).warn("Bean not available in CLI mode: " + beanClass.getName() + ". Error: " + e.getMessage());
      return null;
    }
  }
}
