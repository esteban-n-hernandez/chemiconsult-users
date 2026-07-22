package com.chemiconsult.repository;

import com.chemiconsult.entity.TaskDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskDE, Long> {

    List<TaskDE> findByArchivedFalse();

    List<TaskDE> findByArchivedTrue();

    List<TaskDE> findByArchivedFalseAndStatus(com.chemiconsult.enums.TaskStatusEnum status);

}
