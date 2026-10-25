package com.gw.api.v1;

import com.gw.api.v1.dto.AgentErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/** Every Agent API error is JSON {@code {"error":code,"message":text}}; never HTML or a redirect. */
@RestControllerAdvice(
    assignableTypes = {
      AgentApiController.class,
      AgentHostController.class,
      AgentRunHistoryController.class,
      AgentWorkflowToolsController.class,
      AgentResultsController.class
    })
public class AgentApiExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(AgentApiExceptionHandler.class);

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<AgentErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getMostSpecificCause();
    String detail = cause == null || cause.getMessage() == null ? "" : cause.getMessage();
    int nl = detail.indexOf('\n');
    if (nl > 0) {
      detail = detail.substring(0, nl);
    }
    return ResponseEntity.badRequest()
        .body(new AgentErrorResponse("bad_request", "Request body is not valid JSON for this endpoint: " + detail));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<AgentErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(new AgentErrorResponse("unsupported_media_type", "Send Content-Type: application/json"));
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<AgentErrorResponse> handleBadParam(Exception ex) {
    return ResponseEntity.badRequest().body(new AgentErrorResponse("bad_request", ex.getMessage()));
  }

  /** Last resort: log server-side, never leak stack traces or paths to the caller. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<AgentErrorResponse> handleUnexpected(Exception ex) {
    logger.error("Agent API unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new AgentErrorResponse("internal_error", "Unexpected server error; see Geoweaver log"));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<AgentErrorResponse> handleStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String msg = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
    String code =
        switch (status) {
          case UNAUTHORIZED -> "unauthorized";
          case FORBIDDEN -> "forbidden";
          case NOT_FOUND -> "not_found";
          case TOO_MANY_REQUESTS -> "rate_limited";
          case BAD_REQUEST -> "bad_request";
          case CONFLICT -> "conflict";
          case BAD_GATEWAY -> "host_unreachable";
          default -> "error";
        };
    return ResponseEntity.status(status).body(new AgentErrorResponse(code, msg));
  }
}
