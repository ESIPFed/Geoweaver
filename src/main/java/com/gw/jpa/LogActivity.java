package com.gw.jpa;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class LogActivity {

    @Id
    private String id;
    private String operator;
    private String category;
    private String objectid;
    private String objname;
    private String operation;
}
