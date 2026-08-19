package com.likelion.routineeatbe.domain.userStatistics.service;

import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsResDto;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsFoodIngredient;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsRecipe;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatistics;
import com.likelion.routineeatbe.domain.userStatistics.exception.UserStatisticsErrorCode;
import com.likelion.routineeatbe.domain.userStatistics.mapper.UserStatisticsMapper;
import com.likelion.routineeatbe.domain.userStatistics.repository.UserStatisticsRepository;
import com.likelion.routineeatbe.domain.userStatistics.repository.UserStatisticsFoodIngredientRepository;
import com.likelion.routineeatbe.domain.userStatistics.repository.UserStatisticsRecipeRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatisticsService {

    private final CookingRecordRepository cookingRecordRepository;
    private final UserStatisticsRepository userStatisticsRepository;
    private final UserRepository userRepository;
    private final UserStatisticsRecipeRepository userStatisticsRecipeRepository;
    private final UserStatisticsFoodIngredientRepository userStatisticsFoodIngredientRepository;
    private final UserStatisticsMapper userStatisticsMapper;

    /**
     * 사용자의 요리 기록을 기반으로 통계를 생성해 비동기로 저장합니다.
     *
     * @param user 통계를 생성할 사용자
     */
    @Async("userStatisticsTaskExecutor")
    @Transactional
    public void saveUserStatistics(User user) {
        log.info(
                "[UserStatisticsService] 사용자 요리 통계 저장 시작 | saveUserStatistics() - START | userId: {}",
                user.getId()
        );

        List<Recipe> recipes = cookingRecordRepository.findDistinctRecipesByUserId(user.getId());
        List<FoodIngredient> foodIngredients =
                cookingRecordRepository.findTop5MostCookedFoodIngredientsByUserId(user.getId());
        List<DifficultyLevel> difficultyLevels =
                cookingRecordRepository.findRecent5MenuDifficultyLevelsByUserId(user.getId());

        DifficultyLevel averageDifficultyLevel = calculateAverageDifficultyLevel(difficultyLevels);
        UserStatistics userStatistics = UserStatistics.create(user, averageDifficultyLevel);
        recipes.forEach(userStatistics::addRecipe);
        foodIngredients.forEach(userStatistics::addFoodIngredient);

        UserStatistics savedStatistics = userStatisticsRepository.save(userStatistics);
        log.info(
                "[UserStatisticsService] 사용자 요리 통계 저장 종료 | saveUserStatistics() - END | userStatisticsId: {}, recipeCount: {}, foodIngredientCount: {}, averageDifficultyLevel: {}",
                savedStatistics.getId(),
                recipes.size(),
                foodIngredients.size(),
                averageDifficultyLevel
        );
    }

    /**
     * 사용자 소유의 통계 스냅샷을 조회합니다.
     *
     * (1) 작업 목적
     * - 사용자 PK와 통계 PK를 검증하여 사용자 통계 조회 권한을 확인합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 존재 여부를 확인합니다.
     * - 사용자와 통계 PK가 일치하는 통계만 조회합니다.
     * - 통계에 연결된 레시피와 음식 재료를 조회하여 응답 DTO로 변환합니다.
     *
     * @param userId 사용자 PK
     * @param statisticsId 통계 PK
     * @return 사용자 통계 조회 응답 DTO
     */
    @Transactional(readOnly = true)
    public UserStatisticsResDto getUserStatistics(Long userId, Long statisticsId) {
        log.info(
                "[UserStatisticsService] 사용자 통계 조회 시작 | getUserStatistics() - START | userId: {}, statisticsId: {}",
                userId,
                statisticsId
        );

        /*
            1. 사용자 존재 여부 확인
            - 사용자가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserStatisticsErrorCode.USER_NOT_FOUND));

        /*
            2. 사용자 소유 통계 조회
            - 통계 PK와 사용자 PK가 모두 일치하는 통계만 조회합니다.
            - 다른 사용자의 통계에는 접근할 수 없습니다.
         */
        UserStatistics userStatistics = userStatisticsRepository
                .findByIdAndUser_Id(statisticsId, userId)
                .orElseThrow(() -> new CustomException(
                        UserStatisticsErrorCode.USER_STATISTICS_NOT_FOUND
                ));

        /*
            3. 통계 하위 데이터 조회
            - 레시피와 메뉴, 음식 재료를 EntityGraph로 함께 조회합니다.
         */
        List<UserStatisticsRecipe> statisticsRecipes = userStatisticsRecipeRepository
                .findAllByUserStatistics_IdOrderByRecipe_IdAsc(userStatistics.getId());
        List<UserStatisticsFoodIngredient> statisticsFoodIngredients =
                userStatisticsFoodIngredientRepository
                        .findAllByUserStatistics_IdOrderByFoodIngredient_IdAsc(
                                userStatistics.getId()
                        );

        /*
            4. 응답 DTO 변환
            - 조회한 통계 Entity와 하위 데이터를 Mapper를 통해 응답 DTO로 변환합니다.
         */
        UserStatisticsResDto result = userStatisticsMapper.toUserStatisticsResDto(
                userStatistics,
                statisticsRecipes,
                statisticsFoodIngredients
        );

        log.info(
                "[UserStatisticsService] 사용자 통계 조회 종료 | getUserStatistics() - END | userId: {}, statisticsId: {}, recipeCount: {}, foodIngredientCount: {}",
                userId,
                statisticsId,
                result.recipeReport().count(),
                result.mostUsedFoodIngredientList().size()
        );
        return result;
    }

    /**
     * 메뉴 난이도 목록의 평균을 DifficultyLevel로 변환합니다.
     *
     * @param difficultyLevels 메뉴 난이도 목록
     * @return 반올림된 평균 난이도
     */
    private DifficultyLevel calculateAverageDifficultyLevel(List<DifficultyLevel> difficultyLevels) {
        log.debug(
                "[UserStatisticsService] 평균 난이도 계산 시작 | calculateAverageDifficultyLevel() - START | difficultyLevelCount: {}",
                difficultyLevels.size()
        );
        int averageScore = (int) Math.round(difficultyLevels.stream()
                .mapToInt(difficultyLevel -> difficultyLevel.ordinal() + 1)
                .average()
                .orElse(1));
        DifficultyLevel result = DifficultyLevel.fromScore(averageScore);
        log.debug(
                "[UserStatisticsService] 평균 난이도 계산 종료 | calculateAverageDifficultyLevel() - END | result: {}",
                result
        );
        return result;
    }
}
