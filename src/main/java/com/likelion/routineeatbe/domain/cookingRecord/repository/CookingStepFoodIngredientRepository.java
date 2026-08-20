package com.likelion.routineeatbe.domain.cookingRecord.repository;

import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingStepFoodIngredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CookingStepFoodIngredientRepository extends
        JpaRepository<CookingStepFoodIngredient, Long> {

    /**
     * 요리 단계에 연결된 요리 기록 음식 재료와 음식 재료 정보를 함께 조회합니다.
     *
     * @param cookingStepId 요리 단계 PK
     * @return 요리 기록 음식 재료 PK 순으로 정렬된 단계별 음식 재료 목록
     */
    @Query("""
            select cookingStepFoodIngredient
            from CookingStepFoodIngredient cookingStepFoodIngredient
            join fetch cookingStepFoodIngredient.cookingRecordFoodIngredient recordFoodIngredient
            join fetch recordFoodIngredient.foodIngredient foodIngredient
            where cookingStepFoodIngredient.cookingStep.id = :cookingStepId
            order by recordFoodIngredient.id
            """)
    List<CookingStepFoodIngredient>
            findAllWithCookingRecordFoodIngredientByCookingStepId(
                    @Param("cookingStepId") Long cookingStepId
            );
}
