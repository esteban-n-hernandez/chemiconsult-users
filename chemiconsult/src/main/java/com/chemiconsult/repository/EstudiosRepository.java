package com.chemiconsult.repository;

import com.chemiconsult.entity.AnalisisDE;
import com.chemiconsult.entity.UserDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface EstudiosRepository extends JpaRepository<AnalisisDE, Long> {

    List<AnalisisDE> findAllByUser(UserDE user);
    
}