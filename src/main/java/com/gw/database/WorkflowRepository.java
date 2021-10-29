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

	@Query(value="select * from workflow where owner = ?1 and confidential = 'TRUE'",
		nativeQuery=true)
	Collection<Workflow> findAllPrivateByOwner(String owner);

	@Query(value="select * from workflow where confidential = 'FALSE'",
		nativeQuery=true)
	Collection<Workflow> findAllPublic();
}
