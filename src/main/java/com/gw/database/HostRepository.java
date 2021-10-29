package com.gw.database;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.gw.jpa.Host;

public interface HostRepository extends CrudRepository<Host, String>{
	
	@Query(value="select * from host where owner = ?1 ",
		nativeQuery = true)
	Collection<Host> findByOwner(String owner);
	
	@Query(value="select * from HOST where name like CONCAT('%',:keyword,'%')",
		nativeQuery = true)
	Collection<Host> findHostsByNameAlike(@Param("keyword") String keyword);
	
	@Query(value="select * from HOST where type = 'ssh'",
		nativeQuery = true)
	Collection<Host> findSSHHosts();

	@Query(value="select * from HOST where owner  = ?1 ",
		nativeQuery = true)
	Collection<Host> findAllPublicAndPrivateByOwner(String owner);

	@Query(value="select * from HOST where owner  = ?1 and confidential = 'TRUE'",
		nativeQuery = true)
	Collection<Host> findPrivateByOwner(String owner);

	@Query(value="select * from HOST where confidential = 'FALSE' ",
		nativeQuery = true)
	Collection<Host> findAllPublicHosts();

	@Query(value="select * from HOST where type = 'jupyter'",
		nativeQuery = true)
	Collection<Host> findJupyterNotebookHosts();
	
	@Query(value="select * from HOST where type = 'jupyterhub'",
	nativeQuery = true)
	Collection<Host> findJupyterHubHosts();

	@Query(value="select * from HOST where type = 'jupyterlab'",
	nativeQuery = true)
	Collection<Host> findJupyterLabHosts();

	@Query(value="select * from HOST where type = 'gee'",
	nativeQuery = true)
	Collection<Host> findGEEHosts();

}
