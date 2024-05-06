package com.gw.database;

import com.gw.jpa.Environment;
import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

/**
 * The EnvironmentRepository interface provides methods for querying environment configurations from
 * a database. It extends the CrudRepository interface to handle database operations for the
 * Environment entity.
 */
@Transactional
public interface EnvironmentRepository extends CrudRepository<Environment, String> {

  /**
   * Find environment configurations based on the provided host ID, binary path, Python environment,
   * and base directory.
   *
   * @param hostid The ID of the host.
   * @param bin The binary path.
   * @param pyenv The Python environment.
   * @param basedir The base directory.
   * @return A collection of environment configurations matching the specified criteria.
   */
  @Query("SELECT e FROM Environment e WHERE e.hostobj.id = :hostid AND e.bin = :bin AND e.pyenv = :pyenv AND e.basedir = :basedir")
  Collection<Environment> findEnvByID_BIN_ENV_BaseDir(String hostid, String bin, String pyenv, String basedir);

  /**
   * Find environment configurations based on the provided host ID and binary path.
   *
   * @param hostid The ID of the host.
   * @param bin The binary path.
   * @return A collection of environment configurations matching the specified host and binary path.
   */
  @Query("SELECT e FROM Environment e WHERE e.hostobj.id = :hostid AND e.bin = :bin")
  Collection<Environment> findEnvByID_BIN(String hostid, String bin);

  /**
   * Find environment configurations based on the provided host ID.
   *
   * @param hostid The ID of the host.
   * @return A collection of environment configurations associated with the specified host.
   */
  @Query("SELECT e FROM Environment e WHERE e.hostobj.id = :hostid")
  Collection<Environment> findEnvByHost(String hostid);

}
