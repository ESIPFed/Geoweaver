package com.gw.jpa;


import lombok.Data;

import java.time.LocalDate;

import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class History {

	@Id
	private String history_id;

	@Lob
	private String history_input;

	@Lob
	private String history_output;

	private LocalDate history_begin_time;

	private LocalDate history_end_time;

	private String history_notes;

	private String history_process;

	private String host_id;

	private String indicator;
}

