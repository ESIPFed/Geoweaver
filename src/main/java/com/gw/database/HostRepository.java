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
	
	
	
}
