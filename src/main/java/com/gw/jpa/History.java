package com.gw.jpa;


import lombok.Data;

import com.gw.utils.RunLogStore;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Index;
import jakarta.persistence.PostRemove;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Date;

@Entity
@Data
// Indexes for the common lookups (runs of one process or workflow, newest runs, runs by status),
// so these stay fast as the history grows. Only indexes are added; columns are unchanged.
@Table(
    indexes = {
      @Index(name = "idx_history_process_time", columnList = "history_process, history_begin_time"),
      @Index(name = "idx_history_begin_time", columnList = "history_begin_time"),
      @Index(name = "idx_history_indicator", columnList = "indicator")
    })
@EntityListeners(History.LogFileCleanup.class)
public class History {

	/** Removes the log file of a deleted run (large logs are kept outside the database). */
	public static class LogFileCleanup {
		@PostRemove
		public void removed(History h) {
			RunLogStore.delete(h.getHistory_id());
		}
	}

	@Id
	private String history_id;

	@Lob
	@JdbcTypeCode(SqlTypes.CLOB)
	private String history_input;

	@Lob
	@JdbcTypeCode(SqlTypes.CLOB)
	private String history_output;

	private Date history_begin_time;

	private Date history_end_time;

	@Lob
	@JdbcTypeCode(SqlTypes.CLOB)
	private String history_notes;

	private String history_process;

	private String host_id;

	private String indicator;
}
