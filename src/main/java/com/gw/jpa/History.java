package com.gw.jpa;


import lombok.Data;


import jakarta.persistence.Lob;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

	private String history_notes;

	private String history_process;

	private String host_id;

	private String indicator;
}
