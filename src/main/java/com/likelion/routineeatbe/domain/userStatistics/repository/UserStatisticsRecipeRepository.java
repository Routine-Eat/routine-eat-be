package com.likelion.routineeatbe.domain.userStatistics.repository;

import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsRecipe;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatisticsRecipeRepository
        extends JpaRepository<UserStatisticsRecipe, Long> {

    /**
     * 통계에 포함된 레시피와 메뉴를 함께 조회합니다.
     *
     * @param statisticsId 통계 PK
     * @return 통계 레시피 목록
     */
    @EntityGraph(attributePaths = {"recipe", "recipe.menu"})
    List<UserStatisticsRecipe> findAllByUserStatistics_IdOrderByRecipe_IdAsc(Long statisticsId);
}
