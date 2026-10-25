package com.gw.api.v1;

import com.gw.api.v1.service.AgentResultsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Files that processes saved in Geoweaver's results folder (the GUI result browser). */
@RestController
@RequestMapping("/api/v1/results")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Tag(name = "Agent API: results")
public class AgentResultsController {

  private final AgentResultsService resultsService;

  public AgentResultsController(AgentResultsService resultsService) {
    this.resultsService = resultsService;
  }

  @GetMapping
  @Operation(summary = "List one folder of results (path is relative; default the top folder)")
  public Map<String, Object> list(@RequestParam(value = "path", required = false) String path) {
    return resultsService.list(path);
  }

  @GetMapping("/file")
  @Operation(summary = "Download one result file (always as an attachment)")
  public ResponseEntity<Resource> file(@RequestParam("path") String path) {
    Path f = resultsService.file(path);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(f.getFileName().toString()).build().toString())
        .body(new FileSystemResource(f));
  }
}
