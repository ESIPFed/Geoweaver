package com.gw.database;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.gw.jpa.GWProcess;
import com.gw.jpa.Host;

/**
 * The ProcessRepository interface provides methods for querying process information from a database.
 * It extends the CrudRepository interface to handle database operations for the GWProcess entity.
 */
public interface ProcessRepository extends CrudRepository<GWProcess, String> {

    /**
     * Find processes whose names are similar to the provided keyword.
     *
     * @param keyword The keyword to search for in process names.
     * @return A collection of processes with names similar to the provided keyword.
     */
    @Query(value = "select * from GWProcess where name like CONCAT('%',:keyword,'%')", nativeQuery = true)
    Collection<GWProcess> findProcessesByNameAlike(@Param("keyword") String keyword);

    /**
     * Find processes written in the Python language.
     *
     * @return A collection of processes implemented in the Python language.
     */
    @Query(value = "select * from gwprocess where lang = 'python'", nativeQuery = true)
    Collection<GWProcess> findPythonProcess();

    /**
     * Find processes owned by the specified user and marked as confidential.
     *
     * @param owner The owner's username.
     * @return A collection of private processes owned by the specified user.
     */
    @Query(value = "select * from gwprocess where owner = ?1 and confidential = 'TRUE'", nativeQuery = true)
    Collection<GWProcess> findAllPrivateByOwner(String owner);

    /**
     * Find processes that are marked as public (not confidential).
     *
     * @return A collection of public processes.
     */
    @Query(value = "select * from gwprocess where confidential = 'FALSE'", nativeQuery = true)
    Collection<GWProcess> findAllPublic();

    /**
     * Find processes written in the Shell language.
     *
     * @return A collection of processes implemented in the Shell language.
     */
    @Query(value = "select * from gwprocess where lang = 'shell'", nativeQuery = true)
    Collection<GWProcess> findShellProcess();

    /**
     * Find processes written in a built-in language or system.
     *
     * @return A collection of processes implemented using a built-in language or system.
     */
    @Query(value = "select * from gwprocess where lang = 'builtin'", nativeQuery = true)
    Collection<GWProcess> findBuiltinProcess();

    /**
     * Find processes implemented as Jupyter notebooks.
     *
     * @return A collection of processes represented as Jupyter notebooks.
     */
    @Query(value = "select * from gwprocess where lang = 'jupyter'", nativeQuery = true)
    Collection<GWProcess> findNotebookProcess();
}

