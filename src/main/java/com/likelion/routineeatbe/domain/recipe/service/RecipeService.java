package com.likelion.routineeatbe.domain.recipe.service;

import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeWithSimilarRecipes;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeDetailReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeKeywordSearchReqDto;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeDetailResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeIngredientUsageListResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeKeywordSearchResDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.RecipeSearchResponseDto;
import com.likelion.routineeatbe.domain.recipe.dto.response.SimilarRecipeResDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.exception.RecipeErrorCode;
import com.likelion.routineeatbe.domain.recipe.mapper.RecipeMapper;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.response.CursorSliceResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private static final double AMOUNT_EPSILON = 1.0e-9;

    private final FindSimilarRecipeService findSimilarRecipeService;

    private final UserRepository userRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    private final RecipeMapper recipeMapper;

    /**
     * 사용자와 인분 수를 기준으로 레시피 상세 정보를 조회합니다.
     * - 전체 필요 재료에 인분 배율을 적용하고 사용자 보유량을 차감하여 추가 재료와 비용을 계산합니다.
     * - 대상 및 유사 레시피 선정은 FindSimilarRecipeService에 위임합니다.
     *
     * @param recipeId 조회할 레시피 PK
     * @param request 사용자 고유 식별번호와 인분 수
     * @return 레시피 기본 정보, 필요 재료, 추가 재료 및 유사 레시피 응답
     */
    @Transactional(readOnly = true)
    public RecipeDetailResDto getRecipeDetail(Long recipeId, RecipeDetailReqDto request) {
        log.info(
                "[RecipeService] 레시피 상세 조회 | getRecipeDetail() - START | recipeId: {}, userNumber: {}, servings: {}",
                recipeId,
                request.userNumber(),
                request.servings()
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(RecipeErrorCode.USER_NOT_FOUND));

        /*
            2. 대상 및 유사 레시피 조회
            - 재료 차이를 0부터 4까지 확장하여 선정한 최대 3개의 유사 레시피를 함께 조회합니다.
         */
        RecipeWithSimilarRecipes recipeResult =
                findSimilarRecipeService.findRecipeWithSimilarRecipes(recipeId);
        Recipe recipe = recipeResult.recipe();

        /*
            3. 대상 및 유사 레시피의 필요 재료 일괄 조회
            - 모든 레시피 PK를 한 번에 전달하고 레시피별 필요 재료 목록으로 그룹화합니다.
         */
        Set<Long> recipeIds = new LinkedHashSet<>();
        recipeIds.add(recipe.getId());
        recipeResult.similarRecipes().stream()
                .map(Recipe::getId)
                .forEach(recipeIds::add);
        Map<Long, List<RecipeFoodIngredient>> requiredIngredientsByRecipe =
                recipeFoodIngredientRepository.findAllByRecipeIdInWithFoodIngredient(recipeIds).stream()
                        .collect(Collectors.groupingBy(
                                requiredIngredient -> requiredIngredient.getRecipe().getId()
                        ));

        /*
            4. 사용자 보유 재료 수량 집계
            - OWN 관계의 주 단위 수량을 음식 재료 PK별로 합산하며 null 수량은 0으로 처리합니다.
         */
        Map<Long, Double> ownedAmountByFoodIngredient = userFoodIngredientRepository
                .findAllWithFoodIngredientByUserIdAndRelationType(
                        user.getId(),
                        UserFoodIngredientType.OWN
                ).stream()
                .collect(Collectors.toMap(
                        userFoodIngredient -> userFoodIngredient.getFoodIngredient().getId(),
                        userFoodIngredient -> userFoodIngredient.getPrimaryAmountValue() == null
                                ? 0.0
                                : userFoodIngredient.getPrimaryAmountValue(),
                        Double::sum
                ));

        /*
            5. 대상 레시피의 전체 및 추가 재료 계산
            - 필요량에는 인분 배율을 적용하고 추가 재료의 보조 수량은 주 단위 부족 비율에 맞춰 계산합니다.
         */
        List<RecipeIngredientResDto> foodIngredients = new ArrayList<>();
        List<RecipeIngredientResDto> additionalFoodIngredients = new ArrayList<>();
        double additionalFoodIngredientCost = 0.0;
        List<RecipeFoodIngredient> targetIngredients = requiredIngredientsByRecipe.getOrDefault(
                recipe.getId(),
                List.of()
        );
        long requiredIngredientCount = targetIngredients.stream()
                .map(targetIngredient -> targetIngredient.getFoodIngredient().getId())
                .distinct()
                .count();
        long matchedIngredientCount = targetIngredients.stream()
                .map(targetIngredient -> targetIngredient.getFoodIngredient().getId())
                .distinct()
                .filter(ownedAmountByFoodIngredient::containsKey)
                .count();
        for (RecipeFoodIngredient targetIngredient : targetIngredients) {
            double primaryNeedAmount = targetIngredient.getPrimaryNeedAmountValue()
                    * request.servings();
            Double secondaryNeedAmount = targetIngredient.getSecondaryNeedAmountValue() == null
                    ? null
                    : targetIngredient.getSecondaryNeedAmountValue() * request.servings();
            foodIngredients.add(recipeMapper.toRecipeIngredientResDto(
                    targetIngredient,
                    primaryNeedAmount,
                    secondaryNeedAmount
            ));

            Long foodIngredientId = targetIngredient.getFoodIngredient().getId();
            double shortageAmount = Math.max(
                    primaryNeedAmount
                            - ownedAmountByFoodIngredient.getOrDefault(foodIngredientId, 0.0),
                    0.0
            );
            if (shortageAmount > AMOUNT_EPSILON) {
                double shortageRatio = primaryNeedAmount > AMOUNT_EPSILON
                        ? shortageAmount / primaryNeedAmount
                        : 0.0;
                Double secondaryShortageAmount = secondaryNeedAmount == null
                        ? null
                        : secondaryNeedAmount * shortageRatio;
                additionalFoodIngredients.add(recipeMapper.toRecipeIngredientResDto(
                        targetIngredient,
                        shortageAmount,
                        secondaryShortageAmount
                ));
                additionalFoodIngredientCost += shortageAmount
                        * targetIngredient.getFoodIngredient().getPricePerHundred()
                        / 100.0;
            }
        }

        /*
            6. 유사 레시피별 추가 재료 개수 계산
            - 각 유사 레시피는 사용자 보유량을 독립적으로 차감하여 부족 재료 개수를 계산합니다.
         */
        List<SimilarRecipeResDto> similarRecipes = recipeResult.similarRecipes().stream()
                .map(similarRecipe -> {
                    long additionalCount = requiredIngredientsByRecipe.getOrDefault(
                                    similarRecipe.getId(),
                                    List.of()
                            ).stream()
                            .filter(requiredIngredient -> {
                                double primaryNeedAmount = requiredIngredient
                                        .getPrimaryNeedAmountValue() * request.servings();
                                double ownedAmount = ownedAmountByFoodIngredient.getOrDefault(
                                        requiredIngredient.getFoodIngredient().getId(),
                                        0.0
                                );
                                return primaryNeedAmount - ownedAmount > AMOUNT_EPSILON;
                            })
                            .count();
                    return recipeMapper.toSimilarRecipeResDto(similarRecipe, additionalCount);
                })
                .toList();

        /*
            7. 상세 응답 변환
            - 계산 결과와 레시피 기본 정보를 Mapper에 전달하여 최종 응답 DTO를 생성합니다.
         */
        RecipeDetailResDto result = recipeMapper.toRecipeDetailResDto(
                recipe,
                matchedIngredientCount,
                requiredIngredientCount,
                (long) Math.ceil(additionalFoodIngredientCost),
                request.servings(),
                foodIngredients,
                additionalFoodIngredients,
                similarRecipes
        );

        log.info(
                "[RecipeService] 레시피 상세 조회 | getRecipeDetail() - END | recipeId: {}, foodIngredientUsingPercent: {}, similarRecipeCount: {}",
                recipeId,
                result.foodIngredientUsingPercent(),
                result.similarRecipes().size()
        );
        return result;
    }

    /**
     * 사용자와 필터 조건을 기준으로 전체 및 추천 유형별 레시피 목록을 조회합니다.
     * - 동일한 위치 커서와 조회 크기를 네 개 목록에 적용합니다.
     * - DEFAULT는 전체, 나머지는 SIMPLE, DIET, GLUTEN_FREE 유형만 조회합니다.
     *
     * @param request 레시피 조회 조건
     * @return 전체 및 추천 유형별 레시피 목록 응답
     */
    @Transactional(readOnly = true)
    public RecipeSearchResponseDto getRecipes(RecipeSearchRequestDto request) {
        log.info(
                "[RecipeService] 전체 레시피 목록 조회 | getRecipes() - START | userNumber: {}, cursor: {}",
                request.userNumber(),
                request.cursor()
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호로 재료 일치도와 부족 재료비를 계산할 사용자를 조회합니다.
         */
        User user = userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(RecipeErrorCode.USER_NOT_FOUND));

        /*
            2. 전체 및 추천 유형별 목록 조회
            - 네 목록은 동일한 필터, 정렬, 커서, 조회 크기를 공유합니다.
         */
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> defaultRecipe = getRecipeSlice(
                user.getId(),
                request,
                RecommendationType.DEFAULT,
                recipeMapper::toRecipeIngredientUsageListResponseDto
        );
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> simpleRecipe = getRecipeSlice(
                user.getId(),
                request,
                RecommendationType.SIMPLE,
                recipeMapper::toRecipeIngredientUsageListResponseDto
        );
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> dietRecipe = getRecipeSlice(
                user.getId(),
                request,
                RecommendationType.DIET,
                recipeMapper::toRecipeIngredientUsageListResponseDto
        );
        CursorSliceResponse<RecipeIngredientUsageListResponseDto> glutenFreeRecipe = getRecipeSlice(
                user.getId(),
                request,
                RecommendationType.GLUTEN_FREE,
                recipeMapper::toRecipeIngredientUsageListResponseDto
        );

        /*
            3. 최상위 응답 조합
            - 조회한 네 CursorSliceResponse를 명세의 응답 필드에 맞게 조합합니다.
         */
        RecipeSearchResponseDto result = RecipeSearchResponseDto.create(
                defaultRecipe,
                simpleRecipe,
                dietRecipe,
                glutenFreeRecipe
        );

        log.info(
                "[RecipeService] 전체 레시피 목록 조회 | getRecipes() - END | default: {}, simple: {}, diet: {}, glutenFree: {}",
                defaultRecipe.content().size(),
                simpleRecipe.content().size(),
                dietRecipe.content().size(),
                glutenFreeRecipe.content().size()
        );
        return result;
    }

    /**
     * 사용자와 메뉴/레시피명 검색어를 기준으로 레시피 목록을 조회합니다.
     * - 사용자 고유 식별번호의 존재 여부를 확인합니다.
     * - 메뉴명 일치도 순으로 조회한 결과를 커서 기반 응답으로 변환합니다.
     *
     * @param request 사용자 식별번호, 검색어 및 커서 조회 조건
     * @return 검색어와 일치하는 레시피 커서 목록
     */
    @Transactional(readOnly = true)
    public CursorSliceResponse<RecipeKeywordSearchResDto> searchRecipesByMenuName(
            RecipeKeywordSearchReqDto request
    ) {
        log.info(
                "[RecipeService] 검색어 기반 레시피 검색 | searchRecipesByMenuName() - START | userNumber: {}, searchWord: {}, cursor: {}",
                request.userNumber(),
                request.searchWord(),
                request.cursor()
        );

        /*
            1. 사용자 존재 여부 확인
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(RecipeErrorCode.USER_NOT_FOUND));

        /*
            2. 검색어 기반 레시피 조회
            - 검색어 앞뒤 공백을 제거하고 메뉴명 일치도 순으로 기본 레시피를 조회합니다.
         */
        Slice<RecipeSearchResult> recipeSlice = recipeRepository.searchRecipesByMenuName(
                        user.getId(),
                        request.searchWord().strip(),
                        request.cursor(),
                        request.size()
                );

        /*
            3. 커서 기반 응답 변환
            - 다음 데이터가 존재하면 다음 조회 위치를 계산하고 Mapper로 응답 DTO를 생성합니다.
         */
        Long nextCursor = recipeSlice.hasNext()
                ? request.cursor() + request.size()
                : null;
        CursorSliceResponse<RecipeKeywordSearchResDto> result = CursorSliceResponse.of(
                recipeSlice,
                recipeMapper::toRecipeKeywordSearchResDto,
                nextCursor
        );

        log.info(
                "[RecipeService] 검색어 기반 레시피 검색 | searchRecipesByMenuName() - END | resultSize: {}, nextCursor: {}",
                result.content().size(),
                result.nextCursor()
        );
        return result;
    }

    /**
     * 하나의 추천 유형에 해당하는 레시피 Slice를 커서 응답으로 변환합니다.
     * @param userId 사용자 ID
     * @param request 레시피 조회 조건
     * @param recommendationType 조회할 추천 유형
     * @param mapper 추천 유형에 맞는 목록 응답 DTO 변환 함수
     * @return 해당 추천 유형의 커서 기반 레시피 목록
     */
    private <T> CursorSliceResponse<T> getRecipeSlice(
            Long userId,
            RecipeSearchRequestDto request,
            RecommendationType recommendationType,
            Function<RecipeSearchResult, T> mapper
    ) {
        log.debug(
                "[RecipeService] 추천 유형별 레시피 조회 | getRecipeSlice() - START | recommendationType: {}",
                recommendationType
        );

        Slice<RecipeSearchResult> recipeSlice = recipeRepository.searchRecipes(
                userId,
                request,
                recommendationType
        );
        Long nextCursor = recipeSlice.hasNext()
                ? request.cursor() + request.size()
                : null;
        CursorSliceResponse<T> result = CursorSliceResponse.of(
                recipeSlice,
                mapper,
                nextCursor
        );

        log.debug(
                "[RecipeService] 추천 유형별 레시피 조회 | getRecipeSlice() - END | resultSize: {}, nextCursor: {}",
                result.content().size(),
                result.nextCursor()
        );
        return result;
    }
}
