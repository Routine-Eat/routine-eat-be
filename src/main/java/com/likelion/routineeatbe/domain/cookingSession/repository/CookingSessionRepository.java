package com.likelion.routineeatbe.domain.cookingSession.repository;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingSessionRepository extends JpaRepository<CookingSession, Long> {
}
