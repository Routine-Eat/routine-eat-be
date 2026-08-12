package com.likelion.routineeatbe.domain.user.repository;

import com.likelion.routineeatbe.domain.user.entity.UserCookingEquipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserCookingEquipmentRepository extends JpaRepository<UserCookingEquipment,Long> {
    // N+1 문제 방지를 위해 CookingEquipment를 Fetch Join하여 한 번에 조회
    @EntityGraph(attributePaths = {"cookingEquipment"})
    List<UserCookingEquipment> findAllByUserId(Long userId);

    // 특정 유저의 선택된 요리도구들 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserCookingEquipment u " +
            "WHERE u.user.id = :userId " +
            "AND u.cookingEquipment.id IN :equipmentIdList")
    void deleteByUserIdAndEquipmentsIds(Long userId,List<Long> equipmentIdList);
}
