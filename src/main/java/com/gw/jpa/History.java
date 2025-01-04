package com.gw.jpa;


import lombok.Data;


import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Data
public class History {

	@Id
	private String history_id;

	@Lob
	private String history_input;

	@Lob
	private String history_output;

	private Date history_begin_time;

	private Date history_end_time;

	@Lob
	private String history_notes;

	private String history_process;

	private String host_id;

	private String indicator;
}
