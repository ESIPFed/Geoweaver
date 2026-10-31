package com.gw.api.v1.service;

import com.gw.utils.BaseTool;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Read-only access to Geoweaver's results folder (where processes save outputs for the GUI result
 * browser). Every path is resolved inside that folder; {@code ..}, absolute paths and symlinks that
 * leave it are refused unless {@code geoweaver.agent.results-follow-symlinks=true} (separate from
 * the GUI's {@code geoweaver.follow_symlinks}).
 */
@Service
public class AgentResultsService {

  static final int MAX_ENTRIES = 1000;

  private final BaseTool baseTool;
  private final boolean followSymlinks;

  public AgentResultsService(
      BaseTool baseTool, @Value("${geoweaver.agent.results-follow-symlinks:false}") boolean followSymlinks) {
    this.baseTool = baseTool;
    this.followSymlinks = followSymlinks;
  }

  public Map<String, Object> list(String path) {
    Path root = root();
    Path dir = resolveInside(root, path);
    if (!Files.isDirectory(dir)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "path is a file; use GET /api/v1/results/file");
    }
    List<Map<String, Object>> entries = new ArrayList<>();
    boolean truncated = false;
    try (Stream<Path> s = Files.list(dir)) {
      List<Path> children = s.sorted(Comparator.comparing(Path::toString)).toList();
      for (Path child : children) {
        if (entries.size() >= MAX_ENTRIES) {
          truncated = true;
          break;
        }
        if (!followSymlinks && Files.isSymbolicLink(child) && !insideReal(root, child)) {
          continue;
        }
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("name", child.getFileName().toString());
        e.put("path", relative(root, child));
        boolean isDir = Files.isDirectory(child);
        e.put("directory", isDir);
        try {
          if (!isDir) {
            e.put("size", Files.size(child));
          }
          e.put("modified", Files.getLastModifiedTime(child).toInstant().toString());
        } catch (IOException ex) {
          e.put("unreadable", true);
        }
        entries.add(e);
      }
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not list results folder");
    }
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("path", relative(root, dir));
    out.put("entries", entries);
    out.put("truncated", truncated);
    return out;
  }

  /** Regular file inside the results folder. */
  public Path file(String path) {
    if (BaseTool.isNull(path)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "path is required");
    }
    Path f = resolveInside(root(), path);
    if (!Files.isRegularFile(f)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "path is not a file");
    }
    return f;
  }

  private Path root() {
    Path root = Paths.get(baseTool.getResultsFolder()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Results folder is not available");
    }
    return root;
  }

  Path resolveInside(Path root, String path) {
    String rel = path == null ? "" : path.trim().replace('\\', '/');
    if (rel.indexOf('\0') >= 0 || rel.startsWith("/") || rel.matches("^[A-Za-z]:.*")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "path must be relative to the results folder");
    }
    Path p = root.resolve(rel).normalize();
    if (!p.startsWith(root)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "path must stay inside the results folder");
    }
    if (!Files.exists(p)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such result: " + rel);
    }
    if (!followSymlinks && !insideReal(root, p)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "path leaves the results folder through a symlink (geoweaver.agent.results-follow-symlinks=false)");
    }
    return p;
  }

  private static boolean insideReal(Path root, Path p) {
    try {
      return p.toRealPath().startsWith(root.toRealPath());
    } catch (IOException e) {
      return false;
    }
  }

  private static String relative(Path root, Path p) {
    return root.relativize(p).toString().replace('\\', '/');
  }
}
