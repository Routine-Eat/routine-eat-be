package com.likelion.routineeatbe.domain.cookingRecord.repository;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingRecordSearchResult;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import java.util.List;
import org.springframework.data.domain.Slice;

public interface CookingRecordRepositoryCustom {

    /**
     * 사용자의 회고 저장까지 종료된 요리 기록을 최신순으로 조회합니다.
     *
     * @param userId 조회할 사용자 PK
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 요리 기록 개수
     * @return 요리 기록 목록 조회 결과 Slice
     */
    Slice<CookingRecordSearchResult> searchTerminatedCookingRecords(
            Long userId,
            Integer cursor,
            Integer size
    );

    /**
     * 사용자가 요리한 레시피를 중복 없이 조회합니다.
     *
     * @param userId 사용자 PK
     * @return 중복 제거된 레시피 목록
     */
    List<Recipe> findDistinctRecipesByUserId(Long userId);

    /**
     * 사용자가 요리한 음식 재료 중 요리 기록별 사용 횟수가 많은 상위 5개를 조회합니다.
     *
     * @param userId 사용자 PK
     * @return 사용 횟수 내림차순 상위 음식 재료 목록
     */
    List<FoodIngredient> findTop5MostCookedFoodIngredientsByUserId(Long userId);

    /**
     * 사용자가 최근 요리한 5개 레시피 메뉴의 난이도 목록을 조회합니다.
     *
     * @param userId 사용자 PK
     * @return 최근 5개 메뉴 난이도 목록
     */
    List<DifficultyLevel> findRecent5MenuDifficultyLevelsByUserId(Long userId);
}
