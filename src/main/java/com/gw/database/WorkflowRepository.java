package com.gw.database;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.gw.jpa.GWProcess;
import com.gw.jpa.Workflow;

public interface WorkflowRepository extends CrudRepository<Workflow, String>{
	
	@Query(value="select * from workflow where name like '%?1%'",
		nativeQuery=true)
	Collection<Workflow> findProcessesByNameAlike(String keyword);
	
}
