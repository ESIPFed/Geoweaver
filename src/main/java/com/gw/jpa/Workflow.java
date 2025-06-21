package com.gw.jpa;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="workflow")
@Getter
@Setter
public class Workflow {

	@Id
	private String id;

	private String name;
	private String description;
	private String owner;
	private String confidential;

	@Lob
	private String edges;

	@Lob
	private String nodes;
}
