package com.likelion.routineeatbe.domain.mealPlan.repository;

import com.likelion.routineeatbe.domain.mealPlan.entity.PlanMenu;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanMenuRepository extends JpaRepository<PlanMenu, Long> {

    /**
     * 완료된 식단 메뉴 ID만 최근 조리일 순으로 조회합니다.
     * 추천 서비스는 이 결과를 사용해 이미 요리한 메뉴를 후순위로 처리합니다.
     */
    @Query("""
            select planMenu.menu.id
            from PlanMenu planMenu
            where planMenu.mealPlan.user.id = :userId
              and planMenu.completed = true
            order by planMenu.date desc, planMenu.id desc
            """)
    List<Long> findCompletedMenuIdsByUserId(@Param("userId") Long userId);

    // 식단 ID 리스트를 받아 한 번에 모든 PlanMenu 조회 (IN 쿼리)
    List<PlanMenu> findByMealPlan_IdIn(List<Long> mealPlanIds);

    @Query("select pm.id from PlanMenu pm where pm.mealPlan.id = :mealPlanId")
    List<Long> findByMealPlan_Id(@Param("mealPlanId") Long mealPlanId);

    @EntityGraph(attributePaths = {"menu"})
    List<PlanMenu> findAllByMealPlan_Id(Long mealPlanId);
}
