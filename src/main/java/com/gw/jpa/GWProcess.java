package com.gw.jpa;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Process POJO
 * 
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

	// true: private; false: public
	private String confidential;
}
