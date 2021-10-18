package com.gw.database;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.gw.jpa.Environment;

public interface EnvironmentRepository extends CrudRepository<Environment, String>{
	
//	new StringBuffer("select * from environment where host = '").append(hostid)
//	.append("' and bin = '").append(bin).append("' and pyenv = '")
//	.append(env).append("' and basedir = '").append(basedir).append("';");
	@Query(value = "select * from environment where hostid = ?1 and bin = ?2 and pyenv = ?3 and basedir = ?4 ",
		nativeQuery = true)
	Collection<Environment> findEnvByID_BIN_ENV_BaseDir(String hostid, String bin, String pyenv, String basedir);

	@Query(value = "select * from environment where hostid = ?1 and bin = ?2",
		nativeQuery = true)
	Collection<Environment> findEnvByID_BIN(String hostid, String bin);
	
	
	@Query(value = "select * from environment where hostid = ?1 ",
		nativeQuery = true)
	Collection<Environment> findEnvByHost(String hostid);
	
}
