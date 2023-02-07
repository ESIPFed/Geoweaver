package com.gw.jpa;


import java.util.Date;

import javax.persistence.Lob;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class History {
	
	/**********************************************/
    /** section of the geoweaver history records **/
    /**********************************************/
	
	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
    private String			 history_id;
	
	@Lob
	@Column(columnDefinition = "LONGTEXT")
    private String			 history_input;
    
    //maximum jupyter notebook 100mb
	@Lob
    @Column(columnDefinition = "LONGTEXT")
    private String			 history_output;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date			 history_begin_time;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date			 history_end_time;
    
    @Column(columnDefinition = "TEXT")
	private String history_notes;

    
    private String			 history_process;
    
    private String           host_id;
    
    private String           indicator;
    
    /**********************************************/
    /** end of history section **/
    /**********************************************/
    

	public String getHistory_notes() {
		return this.history_notes;
	}

	public void setHistory_notes(String history_notes) {
		this.history_notes = history_notes;
	}


	public String getIndicator() {
		return indicator;
	}

	public String getHost_id() {
		return host_id;
	}

	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public String getHistory_input() {
		return history_input;
	}

	public void setHistory_input(String history_input) {
		this.history_input = history_input;
	}

	public String getHistory_output() {
		return history_output;
	}

	public void setHistory_output(String history_output) {
		this.history_output = history_output;
	}
	
	public Date getHistory_begin_time() {
		return history_begin_time;
	}

	public void setHistory_begin_time(Date history_begin_time) {
		this.history_begin_time = history_begin_time;
	}

	public Date getHistory_end_time() {
		return history_end_time;
	}

	public void setHistory_end_time(Date history_end_time) {
		this.history_end_time = history_end_time;
	}

	public String getHistory_process() {
		return history_process;
	}

	public void setHistory_process(String history_process) {
		this.history_process = history_process;
	}

	public String getHistory_id() {
		return history_id;
	}

	public void setHistory_id(String history_id) {
		this.history_id = history_id;
	}
}
