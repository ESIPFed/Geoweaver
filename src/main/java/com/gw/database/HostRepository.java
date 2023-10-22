package com.gw.database;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.gw.jpa.Host;

/**
 * The HostRepository interface provides methods for querying host-related information from the database.
 * It extends the CrudRepository interface to handle basic CRUD operations.
 */
public interface HostRepository extends CrudRepository<Host, String> {

    /**
     * Find hosts by owner.
     *
     * @param owner The owner's name.
     * @return A collection of hosts owned by the specified owner.
     */
    @Query(value = "select * from host where owner = ?1 ", nativeQuery = true)
    Collection<Host> findByOwner(String owner);
	// Collection<Host> findByOwner(String owner);

    /**
     * Find hosts by name containing the specified keyword.
     *
     * @param keyword The keyword to search for in host names.
     * @return A collection of hosts with names containing the keyword.
     */
    @Query(value = "select * from HOST where name like CONCAT('%',:keyword,'%')", nativeQuery = true)
    Collection<Host> findHostsByNameAlike(@Param("keyword") String keyword);
	// Collection<Host> findByNameContaining(String keyword);

    /**
     * Find SSH hosts.
     *
     * @return A collection of SSH hosts.
     */
    @Query(value = "select * from HOST where type = 'ssh'", nativeQuery = true)
    Collection<Host> findSSHHosts();

    /**
     * Find all public and private hosts of an owner.
     *
     * @param owner The owner's name.
     * @return A collection of all public and private hosts owned by the specified owner.
     */
    @Query(value = "select * from HOST where owner  = ?1 ", nativeQuery = true)
    Collection<Host> findAllPublicAndPrivateByOwner(String owner);

    /**
     * Find private hosts of an owner.
     *
     * @param owner The owner's name.
     * @return A collection of private hosts owned by the specified owner.
     */
    @Query(value = "select * from HOST where owner  = ?1 and confidential = 'TRUE'", nativeQuery = true)
    Collection<Host> findPrivateByOwner(String owner);
	// Collection<Host> findByOwnerAndConfidential(String owner, boolean confidential);

    /**
     * Find all public hosts.
     *
     * @return A collection of all public hosts.
     */
    @Query(value = "select * from HOST where confidential = 'FALSE'", nativeQuery = true)
    Collection<Host> findAllPublicHosts();

    /**
     * Find hosts of type 'jupyter'.
     *
     * @return A collection of hosts of type 'jupyter'.
     */
    @Query(value = "select * from HOST where type = 'jupyter'", nativeQuery = true)
    Collection<Host> findJupyterNotebookHosts();

    /**
     * Find hosts of type 'jupyterhub'.
     *
     * @return A collection of hosts of type 'jupyterhub'.
     */
    @Query(value = "select * from HOST where type = 'jupyterhub'", nativeQuery = true)
    Collection<Host> findJupyterHubHosts();

    /**
     * Find hosts of type 'jupyterlab'.
     *
     * @return A collection of hosts of type 'jupyterlab'.
     */
    @Query(value = "select * from HOST where type = 'jupyterlab'", nativeQuery = true)
    Collection<Host> findJupyterLabHosts();

    /**
     * Find hosts of type 'gee'.
     *
     * @return A collection of hosts of type 'gee'.
     */
    @Query(value = "select * from HOST where type = 'gee'", nativeQuery = true)
    Collection<Host> findGEEHosts();
}

