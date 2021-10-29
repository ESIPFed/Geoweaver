package com.gw.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LogActivity {
    
    @Id
    String id;

    String operator;

    String category;

    String objectid;

    String objname;
    
    String operation;

    public String getObjname() {
        return this.objname;
    }

    public void setObjname(String objname) {
        this.objname = objname;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getObjectid() {
        return this.objectid;
    }

    public void setObjectid(String objectid) {
        this.objectid = objectid;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }



}
