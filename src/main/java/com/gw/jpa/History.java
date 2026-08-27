package com.gw.jpa;


import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Date;

@Entity
@Data
public class History {

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
