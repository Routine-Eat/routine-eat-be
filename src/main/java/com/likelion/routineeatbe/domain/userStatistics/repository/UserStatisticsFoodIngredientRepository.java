package com.likelion.routineeatbe.domain.userStatistics.repository;

import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsFoodIngredient;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatisticsFoodIngredientRepository
        extends JpaRepository<UserStatisticsFoodIngredient, Long> {

    /**
     * 통계에 포함된 음식 재료와 음식 재료 정보를 함께 조회합니다.
     *
     * @param statisticsId 통계 PK
     * @return 통계 음식 재료 목록
     */
    @EntityGraph(attributePaths = "foodIngredient")
    List<UserStatisticsFoodIngredient>
    findAllByUserStatistics_IdOrderByFoodIngredient_IdAsc(Long statisticsId);
}
