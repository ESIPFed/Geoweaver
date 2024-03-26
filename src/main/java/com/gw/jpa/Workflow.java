package com.gw.jpa;

import lombok.Data;

import javax.persistence.Lob;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
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
