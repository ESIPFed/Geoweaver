package com.gw.database;

import com.gw.jpa.History;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

/**
 * The HistoryRepository interface provides methods for querying historical execution data (history)
 * from a database. It extends the JpaRepository interface to handle database operations.
 */
@Transactional
public interface HistoryRepository extends JpaRepository<History, String> {

  /**
   * Find recent history records for a specific host, limited by the specified count.
   *
   * @param hostid The ID of the host.
   * @param limit The maximum number of history records to retrieve.
   * @return A collection of recent history records for the host.
   */
  @Query(value = "SELECT * FROM history WHERE host_id = ?1 ORDER BY history_begin_time DESC LIMIT ?2", nativeQuery = true)
  List<History> findRecentHistory(String hostid, int limit);

  /**
   * Find all running history records.
   *
   * @return A list of all running history records.
   */
  @Query(value = "SELECT * FROM history WHERE indicator = 'Running' ORDER BY history_begin_time DESC", nativeQuery = true)
  List<History> findAllRunningResource();

  /**
   * Find all running workflows.
   *
   * @return A list of all running workflow records with additional information.
   */
  @Query(value = "SELECT * FROM history, workflow WHERE history.history_process = workflow.id AND history.indicator = 'Running' ORDER BY history_begin_time DESC", nativeQuery = true)
  List<Object[]> findRunningWorkflow();

  /**
   * Find all failed workflows.
   *
   * @return A list of all failed workflow records with additional information.
   */
  @Query(value = "SELECT * FROM history, workflow WHERE history.history_process = workflow.id AND history.indicator = 'Failed' ORDER BY history_begin_time DESC", nativeQuery = true)
  List<Object[]> findFailedWorkflow();

  /**
   * Find all successfully completed workflows.
   *
   * @return A list of all successful workflow records with additional information.
   */
  @Query(value = "SELECT * FROM history, workflow WHERE history.history_process = workflow.id AND history.indicator = 'Done' ORDER BY history_begin_time DESC", nativeQuery = true)
  List<Object[]> findSuccessWorkflow();

  /**
   * Find all running processes.
   *
   * @return A list of all running process records with additional information.
   */
  @Query(value = "SELECT * FROM history, gwprocess WHERE history.history_process = gwprocess.id AND history.indicator = 'Running' ORDER BY history_begin_time DESC", nativeQuery = true)
  List<Object[]> findRunningProcess();


  /**
   * Find all failed processes.
   *
   * @return A list of all failed process records with additional information.
   */
  @Query(value = "SELECT * FROM history, gwprocess WHERE history.history_process = gwprocess.id AND history.indicator = 'Failed' ORDER BY history_begin_time DESC", nativeQuery = true)
  List<Object[]> findFailedProcess();


  /**
   * Find all successfully completed processes.
   *
   * @return A list of all successful process records with additional information.
   */
  @Query(value = "SELECT * FROM history, gwprocess WHERE history.history_process = gwprocess.id AND history.indicator = 'Done' ORDER BY history_begin_time DESC", nativeQuery = true)
  List<Object[]> findSuccessProcess();

  /**
   * Find history records by process ID.
   *
   * @param pid The ID of the process.
   * @return A list of history records associated with the specified process ID.
   */
  @Query(value = "SELECT * FROM history WHERE history_process = ?1 ORDER BY history_begin_time DESC", nativeQuery = true)
  List<History> findByProcessId(String pid);


  /**
   * Find history records by process ID, excluding 'Skipped' indicator.
   *
   * @param pid The ID of the process.
   * @return A list of history records associated with the specified process ID, excluding 'Skipped'
   *     records.
   */
  @Query(value = "SELECT * FROM history WHERE history_process = ?1 AND history_input != 'No code saved' ORDER BY history_begin_time DESC", nativeQuery = true)
  List<History> findByProcessIdIgnoreUnknown(String pid);


  /**
   * Find history records by workflow ID.
   *
   * @param wid The ID of the workflow.
   * @return A list of history records associated with the specified workflow ID.
   */
  @Query(value = "SELECT * FROM history WHERE history_process = ?1 ORDER BY history_begin_time DESC", nativeQuery = true)
  List<History> findByWorkflowId(String wid);

  /**
   * Find recent workflow records, limited by the specified count.
   *
   * @param limit The maximum number of recent workflow records to retrieve.
   * @return A list of recent workflow records with additional information.
   */
  @Query(value = "SELECT * FROM history, workflow WHERE workflow.id = history.history_process ORDER BY history_begin_time DESC LIMIT ?1", nativeQuery = true)
  List<Object[]> findRecentWorkflow(int limit);


  /**
   * Find recent process records, limited by the specified count.
   *
   * @param limit The maximum number of recent process records to retrieve.
   * @return A list of recent process records with additional information.
   */
  @Query(value = "SELECT * FROM history, gwprocess WHERE gwprocess.id = history.history_process ORDER BY history_begin_time DESC LIMIT ?1", nativeQuery = true)
  List<Object[]> findRecentProcess(int limit);


  /**
   * Find a history record associated with a specific history ID.
   *
   * @param history_id The ID of the history record.
   * @return A list containing a history record and additional information about the associated
   *     process.
   */
  @Query(value = "SELECT * FROM history WHERE history_id = ?1 AND history_process = ?2", nativeQuery = true)
  List<History> findHistoryWithExecutionId(String history_id, String workflowId);


  @Query(value = "SELECT * FROM history, gwprocess WHERE history.history_id = ?1 AND history.history_process = gwprocess.id", nativeQuery = true)
  List<Object[]> findOneHistoryofProcess(String history_id);

}
