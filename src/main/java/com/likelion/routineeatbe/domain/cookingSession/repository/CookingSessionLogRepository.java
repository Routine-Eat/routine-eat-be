package com.likelion.routineeatbe.domain.cookingSession.repository;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSessionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingSessionLogRepository extends
        JpaRepository<CookingSessionLog, Long>,
        CookingSessionLogRepositoryCustom {
}
