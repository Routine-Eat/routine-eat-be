package com.likelion.routineeatbe.domain.recipeCookingEquipment.repository;

import com.likelion.routineeatbe.domain.recipeCookingEquipment.entity.RecipeCookingEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RecipeCookingEquipmentRepository
        extends JpaRepository<RecipeCookingEquipment, Long> {

    /**
     * 전달받은 레시피들의 필수 도구 연결을 도구 엔티티와 함께 일괄 조회합니다.
     * 추천 서비스에서 레시피마다 추가 쿼리를 실행하지 않도록 하기 위한 fetch join 쿼리입니다.
     */
    @Query("""
            select recipeCookingEquipment
            from RecipeCookingEquipment recipeCookingEquipment
            join fetch recipeCookingEquipment.cookingEquipment
            where recipeCookingEquipment.recipe.id in :recipeIds
            """)
    List<RecipeCookingEquipment> findAllByRecipeIdInWithCookingEquipment(
            @Param("recipeIds") Collection<Long> recipeIds
    );
}
