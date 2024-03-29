package com.gw.jpa;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Environment {
	@Id
	private String id;

	private String name, type, bin, pyenv, basedir;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hostid")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Host hostobj;

	@Lob
	private String settings;
}
