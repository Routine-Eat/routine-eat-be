package com.likelion.routineeatbe.domain.user.repository;

import com.likelion.routineeatbe.domain.user.entity.UserCookingEquipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCookingEquipmentRepository extends JpaRepository<UserCookingEquipment,Long> {
    // N+1 문제 방지를 위해 CookingEquipment를 Fetch Join하여 한 번에 조회
    @EntityGraph(attributePaths = {"cookingEquipment"})
    List<UserCookingEquipment> findAllByUserId(Long userId);
}
