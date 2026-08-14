package com.likelion.routineeatbe.domain.user.repository;

import com.likelion.routineeatbe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByLoginNumber(String loginNumber);

    boolean existsByLoginNumber(String loginNumber);

    boolean existsById(Long id);
}
