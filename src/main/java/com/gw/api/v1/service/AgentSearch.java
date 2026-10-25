package com.gw.api.v1.service;

import java.util.Locale;

/** {@code ?q=} matching and the name rule shared by Agent API services. */
final class AgentSearch {

  private AgentSearch() {}

  /**
   * Names show up in the GUI's HTML lists, so markup characters are refused (1-100 characters,
   * no {@code < > " ' `} or control characters).
   */
  static void requireDisplayName(String name, String field) {
    if (name == null
        || name.isBlank()
        || name.length() > 100
        || name.chars().anyMatch(c -> Character.isISOControl(c) || "<>\"'`".indexOf(c) >= 0)) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST,
          field + " must be 1-100 characters without < > \" ' ` or control characters");
    }
  }

  /** Free text (notes, descriptions) is inserted into GUI HTML as is, so tags are refused. */
  static void requireNoMarkup(String text, String field) {
    if (text != null && (text.indexOf('<') >= 0 || text.indexOf('>') >= 0)) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.BAD_REQUEST, field + " must not contain < or >");
    }
  }

  static boolean matches(String query, String... fields) {
    if (query == null || query.isBlank()) {
      return true;
    }
    String q = query.trim().toLowerCase(Locale.ROOT);
    for (String f : fields) {
      if (f != null && f.toLowerCase(Locale.ROOT).contains(q)) {
        return true;
      }
    }
    return false;
  }
}
