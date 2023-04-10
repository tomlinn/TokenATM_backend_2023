package io.renren.modules.tokenatm.service;

import io.renren.modules.tokenatm.entity.TokenCountEntity;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<TokenCountEntity, String> {

}
