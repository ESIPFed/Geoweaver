package com.gw.database;

import com.gw.jpa.WorkspaceFeedEvent;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/** Change notices shared between Geoweaver processes on one database. */
@Transactional
public interface WorkspaceFeedRepository extends CrudRepository<WorkspaceFeedEvent, Long> {

  List<WorkspaceFeedEvent> findTop200ByIdGreaterThanOrderByIdAsc(Long id);

  @Query("select coalesce(max(e.id), 0) from WorkspaceFeedEvent e")
  long maxId();

  @Modifying
  @Query("delete from WorkspaceFeedEvent e where e.createdAt < :before")
  int deleteOlderThan(@Param("before") Date before);
}
