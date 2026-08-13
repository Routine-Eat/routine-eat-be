package com.likelion.routineeatbe.domain.recipe.repository;

import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {

    /**
     * 레시피 PK에 연결된 요리 단계를 단계 번호 오름차순으로 조회합니다.
     *
     * @param recipeId 레시피 PK
     * @return 정렬된 레시피 요리 단계 목록
     */
    List<RecipeStep> findAllByRecipeIdOrderByLevelAsc(Long recipeId);
}
