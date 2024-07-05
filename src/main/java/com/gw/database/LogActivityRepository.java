package com.gw.database;

import com.gw.jpa.LogActivity;
import org.springframework.data.repository.CrudRepository;

import jakarta.transaction.Transactional;

/** Log Activity Repository */
@Transactional
interface LogActivityRepository extends CrudRepository<LogActivity, String> {}
