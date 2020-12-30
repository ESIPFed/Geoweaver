package com.gw.database;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.gw.jpa.GWProcess;
import com.gw.jpa.Host;

public interface ProcessRepository extends CrudRepository<GWProcess, String>{
	
	@Query(value = "select * from GWProcess where name like '%?1%'", 
		nativeQuery = true)
	Collection<GWProcess> findProcessesByNameAlike(String keyword);
	
	@Query(value="select * from gwprocess where description = 'python'",
		nativeQuery = true)
	Collection<GWProcess> findPythonProcess();
	
	
	
}
