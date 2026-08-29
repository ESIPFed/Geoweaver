package com.gw.jpa;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Host {

	@Id
	private String id;

	private String name;
	private String ip;
	private String port;
	private String username;
	private String owner;
	private String type;
	private String url;
	private String confidential;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hostobj")
	private Set<Environment> envs;
}
