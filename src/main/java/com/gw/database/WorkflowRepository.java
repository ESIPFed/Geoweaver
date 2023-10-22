package com.gw.database;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.gw.jpa.GWProcess;
import com.gw.jpa.Workflow;

/**
 * The WorkflowRepository interface provides methods for querying workflow information from a database.
 * It extends the CrudRepository interface to handle database operations for the Workflow entity.
 */
public interface WorkflowRepository extends CrudRepository<Workflow, String> {

    /**
     * Find workflows whose names are similar to the provided keyword.
     *
     * @param keyword The keyword to search for in workflow names.
     * @return A collection of workflows with names similar to the provided keyword.
     */
    @Query(value = "select * from workflow where name like CONCAT('%',:keyword,'%')", nativeQuery = true)
    Collection<Workflow> findProcessesByNameAlike(@Param("keyword") String keyword);

    /**
     * Find workflows owned by the specified user and marked as confidential.
     *
     * @param owner The owner's username.
     * @return A collection of private workflows owned by the specified user.
     */
    @Query(value = "select * from workflow where owner = ?1 and confidential = 'TRUE'", nativeQuery = true)
    Collection<Workflow> findAllPrivateByOwner(String owner);

    /**
     * Find workflows that are marked as public (not confidential).
     *
     * @return A collection of public workflows.
     */
    @Query(value = "select * from workflow where confidential = 'FALSE'", nativeQuery = true)
    Collection<Workflow> findAllPublic();
}

