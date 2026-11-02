package com.gw.api.v1.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.WorkspaceFeedRepository;
import com.gw.jpa.WorkspaceFeedEvent;
import java.util.Date;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Writes change notices to the shared {@code gw_workspace_feed} table, where every Geoweaver
 * server on the same database picks them up and tells its open pages. Used by servers and by the
 * command line.
 */
@Component
public class WorkspaceFeedWriter {

  private static final Logger log = Logger.getLogger(WorkspaceFeedWriter.class);

  /** Identifies this process's rows in the shared feed. */
  public static final String ORIGIN = UUID.randomUUID().toString();

  private final WorkspaceFeedRepository feed;
  private final ObjectMapper mapper = new ObjectMapper();

  public WorkspaceFeedWriter(WorkspaceFeedRepository feed) {
    this.feed = feed;
  }

  public void share(WorkspaceEvent e) {
    try {
      String json = mapper.writeValueAsString(e.withRevision(0));
      if (json.length() > 4000) return; // notices are small; never truncate JSON
      WorkspaceFeedEvent row = new WorkspaceFeedEvent();
      row.setCreatedAt(new Date());
      row.setOrigin(ORIGIN);
      row.setPayload(json);
      feed.save(row);
    } catch (Exception ex) {
      log.debug("Could not share a workspace change: " + ex);
    }
  }
}
