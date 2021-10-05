package com.gw.database;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.gw.jpa.GWProcess;
import com.gw.jpa.Host;

public interface ProcessRepository extends CrudRepository<GWProcess, String>{
	
	@Query(value = "select * from GWProcess where name like CONCAT('%',:keyword,'%')", 
		nativeQuery = true)
	Collection<GWProcess> findProcessesByNameAlike(@Param("keyword") String keyword);
	
	@Query(value="select * from gwprocess where lang = 'python'",
		nativeQuery = true)
	Collection<GWProcess> findPythonProcess();

	@Query(value="select * from gwprocess where owner in ('111111', ?1)",
		nativeQuery = true)
	Collection<GWProcess> findAllPublicPrivateByOwner(String owner);

	@Query(value="select * from gwprocess where lang = 'shell'",
		nativeQuery = true)
	Collection<GWProcess> findShellProcess();
	
	@Query(value="select * from gwprocess where lang = 'builtin'",
		nativeQuery = true)
	Collection<GWProcess> findBuiltinProcess();

	@Query(value="select * from gwprocess where lang = 'jupyter'",
		nativeQuery = true)
	Collection<GWProcess> findNotebookProcess();


	
}
