package io.renren.modules.tokenatm.service;

import io.renren.modules.tokenatm.entity.RequestEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RequestRepository extends CrudRepository<RequestEntity, Integer> {
        List<RequestEntity> findAllByOrderByIdDescStatusAsc();
        List<RequestEntity> findByStudentIdAndAssignmentIdOrderByIdDesc(String user_id, String assignment);
        List<RequestEntity> findAllByStatusOrderByIdDesc(String status);

}
