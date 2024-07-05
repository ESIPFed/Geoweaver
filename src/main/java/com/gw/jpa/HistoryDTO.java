package com.gw.jpa;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class HistoryDTO {
    @Id
    private String history_id;
    private Date history_begin_time;
    private Date history_end_time;
    private String history_notes;
    private String history_process;
    private String host_id;
    private String indicator;

    public HistoryDTO(String history_id, Date history_begin_time, Date history_end_time, String history_notes, String history_process, String host_id, String indicator) {
        this.history_id = history_id;
        this.history_begin_time = history_begin_time;
        this.history_end_time = history_end_time;
        this.history_notes = history_notes;
        this.history_process = history_process;
        this.host_id = host_id;
        this.indicator = indicator;
    }
}

