package com.gw.database;

import com.gw.jpa.GWUser;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

@Transactional
public interface UserRepository extends CrudRepository<GWUser, String> {}
