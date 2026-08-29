package com.gw.jpa;

import lombok.Data;

import jakarta.persistence.Lob;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
