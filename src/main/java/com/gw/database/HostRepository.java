package com.gw.database;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.gw.jpa.Host;

public interface HostRepository extends CrudRepository<Host, String>{
	
	@Query(value="select * from hosts where owner = ?1 ",
		nativeQuery = true)
	Collection<Host> findByOwner(String owner);
	
	@Query(value="select * from hosts where name like '%?1%'",
		nativeQuery = true)
	Collection<Host> findHostsByNameAlike(String keyword);
	
	
	
}
