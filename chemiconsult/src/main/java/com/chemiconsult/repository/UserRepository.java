package com.chemiconsult.repository;

import com.chemiconsult.entity.UserDE;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDE,Long> {

    Optional<UserDE> findByUsername(String username);

    Optional<UserDE> findByEmail(String email);

    boolean existsByUsername(String username);
}
