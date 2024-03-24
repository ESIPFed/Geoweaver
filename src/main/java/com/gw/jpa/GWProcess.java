package com.gw.jpa;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.Id;

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
	@Column(columnDefinition = "LONGTEXT")
	private String description;

	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String code;

	private String lang;

	private String owner;

	//true: private; false: public
	private String confidential;
}

