package com.gw.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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

	@Column(columnDefinition = "LONGTEXT")
	private String settings;
}
