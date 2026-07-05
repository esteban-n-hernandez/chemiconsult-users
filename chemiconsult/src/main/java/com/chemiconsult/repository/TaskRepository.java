package com.chemiconsult.repository;

import com.chemiconsult.entity.TaskDE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<TaskDE, Long> {
}
