package com.gw.database;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.gw.jpa.GWProcess;
import com.gw.jpa.Workflow;

public interface WorkflowRepository extends CrudRepository<Workflow, String>{
	
	@Query(value="select * from workflow where name like CONCAT('%',:keyword,'%')",
		nativeQuery=true)
	Collection<Workflow> findProcessesByNameAlike(@Param("keyword") String keyword);

	@Query(value="select * from workflow where owner in ('111111', ?1)",
		nativeQuery=true)
	Collection<Workflow> findAllPublicPrivateByOwner(String owner);
}
