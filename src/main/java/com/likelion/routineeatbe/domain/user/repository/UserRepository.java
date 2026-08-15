package com.likelion.routineeatbe.domain.user.repository;

import com.likelion.routineeatbe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByLoginNumber(String loginNumber);

    boolean existsByLoginNumber(String loginNumber);

    /**
     * 동시 요리 시작 요청을 직렬화하기 위해 사용자 행을 비관적 쓰기 잠금으로 조회합니다.
     *
     * @param userId 사용자 PK
     * @return 잠금이 적용된 사용자
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);

    boolean existsById(Long id);
}
