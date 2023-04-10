package io.renren.modules.tokenatm.service;

import io.renren.modules.tokenatm.entity.ConfigEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

// This will be AUTO IMPLEMENTED by Spring into a Bean called LogRepository
// CRUD refers Create, Read, Update, Delete

public interface ConfigRepository extends CrudRepository<ConfigEntity, Integer> {

    @Query("SELECT l.config_name from ConfigEntity l WHERE l.config_type = ?1")
    List<String> findByType(String config_type);
}
