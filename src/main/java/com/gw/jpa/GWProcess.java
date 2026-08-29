package com.gw.jpa;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Lob;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Process POJO
 * @author jensensun
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class GWProcess {

	@Id
	private String id;

	private String name;

	@Lob
	private String description;

	@Lob
	private String code;

	private String lang;

	private String owner;

	//true: private; false: public
	private String confidential;
}

