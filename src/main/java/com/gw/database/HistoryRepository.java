package com.gw.database;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gw.jpa.History;

public interface HistoryRepository extends JpaRepository<History, String>{

	@Query(value="select * from history where host_id = ?1 ORDER BY history_begin_time DESC limit ?2",
		nativeQuery = true)
	Collection<History> findRecentHistory(String hostid, int limit);
	
	@Query(value="select * from history where indicator='Running'  ORDER BY history_begin_time DESC;",
		nativeQuery = true)
	List<History> findAllRunningResource();
	
	@Query(value="select * from history, workflow where history.history_process = workflow.id and history.indicator = 'Running' ORDER BY history_begin_time DESC;",
		nativeQuery = true)
	List<Object[]> findRunningWorkflow();

	@Query(value="select * from history, workflow where history.history_process = workflow.id and history.indicator = 'Failed' ORDER BY history_begin_time DESC;",
		nativeQuery = true)
	List<Object[]> findFailedWorkflow();

	@Query(value="select * from history, workflow where history.history_process = workflow.id and history.indicator = 'Done' ORDER BY history_begin_time DESC;",
		nativeQuery = true)
	List<Object[]> findSuccessWorkflow();
	
	@Query(value="select * from history, gwprocess where history.history_process = gwprocess.id and history.indicator = 'Running' ORDER BY history_begin_time DESC;",
		nativeQuery = true)
	List<Object[]> findRunningProcess();

	@Query(value="select * from history, gwprocess where history.history_process = gwprocess.id and history.indicator = 'Failed' ORDER BY history_begin_time DESC;",
		nativeQuery = true)
	List<Object[]> findFailedProcess();

	@Query(value="select * from history, gwprocess where history.history_process = gwprocess.id and history.indicator = 'Done' ORDER BY history_begin_time DESC;",
		nativeQuery = true)
	List<Object[]> findSuccessProcess();
	
	@Query(value="select * from history where history.history_process = ?1 ORDER BY history_begin_time DESC;",
			nativeQuery = true)
	List<History> findByProcessId(String pid);

	@Query(value="select * from history where history.history_process = ?1 and history.indicator != 'Skipped' ORDER BY history_begin_time DESC;",
			nativeQuery = true)
	List<History> findByProcessIdIgnoreUnknown(String pid);

	@Query(value="select * from history where history.history_process = ?1 ORDER BY history_begin_time DESC;",
			nativeQuery = true)
	List<History> findByWorkflowId(String wid);
	
	
	@Query(value="select * from history, workflow where workflow.id = history.history_process ORDER BY history_begin_time DESC limit ?1",
		nativeQuery = true)
	List<Object[]> findRecentWorkflow(int limit);
	
	
	@Query(value="select * from history, gwprocess where gwprocess.id = history.history_process ORDER BY history_begin_time DESC limit ?1",
		nativeQuery = true)
	List<Object[]> findRecentProcess(int limit);
	
	@Query(value="select * from history, gwprocess where history.history_id = ?1 and history.history_process=gwprocess.id",
		nativeQuery = true)
	List<Object[]> findOneHistoryofProcess(String history_id);

	
	
	
	
}
