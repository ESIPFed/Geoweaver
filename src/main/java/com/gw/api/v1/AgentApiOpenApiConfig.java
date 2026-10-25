package com.gw.api.v1;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes the Agent API schema at {@code /v3/api-docs/agent-api} (public, no secrets) so coding
 * agents can discover request/response shapes. The full {@code all} group stays behind GUI login.
 */
@Configuration
public class AgentApiOpenApiConfig {

  @Bean
  public GroupedOpenApi agentApiGroup() {
    return GroupedOpenApi.builder().group("agent-api").pathsToMatch("/api/v1/**").build();
  }

  @Bean
  public GroupedOpenApi allApiGroup() {
    return GroupedOpenApi.builder().group("all").pathsToMatch("/**").build();
  }
}
