package com.gw.jpa;

import lombok.Data;

import java.util.Date;

import javax.persistence.Lob;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Data
public class History {

	@Id
	private String history_id;

	@Lob
	private String history_input;

	@Lob
	private String history_output;

	@Temporal(TemporalType.TIMESTAMP)
	private Date history_begin_time;

	@Temporal(TemporalType.TIMESTAMP)
	private Date history_end_time;

	@Lob
	private String history_notes;

	private String history_process;

	private String host_id;

	private String indicator;
}
