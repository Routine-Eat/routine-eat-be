package com.likelion.routineeatbe.domain.recipe.service;

import com.likelion.routineeatbe.domain.mealPlan.exception.MealPlanErrorCode;
import com.likelion.routineeatbe.domain.mealPlan.repository.PlanMenuRepository;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import com.likelion.routineeatbe.domain.recipe.dto.gemini.RecipeReRecommendFunctionDeclaration;
import com.likelion.routineeatbe.domain.recipe.dto.gemini.RecipeReRecommendGeminiResponse;
import com.likelion.routineeatbe.domain.recipe.dto.gemini.RecipeRecommendationFunctionDeclaration;
import com.likelion.routineeatbe.domain.recipe.dto.gemini.RecipeRecommendationGeminiResponse;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeReRecommendRequest;
import com.likelion.routineeatbe.domain.recipe.dto.response.AiRecipeRecommendResponse;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.entity.RecipeCookingEquipment;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.repository.RecipeCookingEquipmentRepository;
import com.likelion.routineeatbe.domain.user.entity.SkillLevel;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.exception.UserFoodIngredientErrorCode;
import com.likelion.routineeatbe.domain.user.repository.UserCookingEquipmentRepository;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.util.GeminiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeAiRecommendService {

    private static final int MAX_AI_CANDIDATES = 30;

    private final UserRepository userRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final UserCookingEquipmentRepository userCookingEquipmentRepository;
    private final RecipeRepository recipeRepository;
    private final MenuRepository menuRepository;
    private final RecipeCookingEquipmentRepository recipeCookingEquipmentRepository;
    private final PlanMenuRepository planMenuRepository;
    private final GeminiUtil geminiUtil;
    private final GeminiProperties geminiProperties;

    /**
     * 사용자 맞춤 단일 레시피 추천
     */
    @Transactional(readOnly = true)
    public AiRecipeRecommendResponse recommendSingleRecipe(Long userId) {

        // 1. 사용자 기본 정보 및 개인화 제약조건(제외/보유 재료, 조리도구, 조리이력) 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        Set<Long> forbiddenIngredientIds = ingredientIds(userId, UserFoodIngredientType.EXCEPTION);
        Set<Long> ownedIngredientIds = ingredientIds(userId, UserFoodIngredientType.OWN);
        Set<Long> ownedEquipmentIds = userCookingEquipmentRepository.findAllByUserId(userId).stream()
                .map(userEquipment -> userEquipment.getCookingEquipment().getId())
                .collect(Collectors.toSet());
        Set<Long> cookedMenuIds = new HashSet<>(planMenuRepository.findCompletedMenuIdsByUserId(userId));

        // 2. DB 동적 필터링을 통해 후보군 조회 (Safety Rule만 적용)
        List<Recipe> filteredRecipes = recipeRepository.findCandidateRecipesByDbFilter(
                forbiddenIngredientIds,
                ownedEquipmentIds,
                null, // difficultyLevel (선택 필터 없음)
                null, // timeFilter (선택 필터 없음)
                null  // desiredIngredientIds (선택 필터 없음)
        );

        if (filteredRecipes.isEmpty()) {
            throw new CustomException(MealPlanErrorCode.NO_RECOMMENDABLE_RECIPE);
        }

        // 3. Candidate DTO 변환 및 우선순위 정렬
        List<Candidate> candidates = filteredRecipes.stream()
                .map(recipe -> Candidate.from(recipe, ownedIngredientIds, cookedMenuIds))
                // [우선순위 정렬] 보유 재료 많은 순 -> 안 해본 요리 -> 난이도 -> 조리시간
                .sorted(Comparator.comparingInt(Candidate::ownedIngredientCount).reversed()
                        .thenComparing(Candidate::cookedBefore)
                        .thenComparingInt(Candidate::difficultyScore)
                        .thenComparingInt(Candidate::timeRequired))
                .limit(MAX_AI_CANDIDATES)
                .toList();

        // 4. Gemini AI 호출 (단 1개 추천 지시)
        RecipeRecommendationGeminiResponse aiResult = geminiUtil.callFunction(
                geminiProperties.menuAnalyzeModel(),
                createPrompt(candidates, user.getSkillLevel()),
                RecipeRecommendationFunctionDeclaration.create(),
                RecipeRecommendationGeminiResponse.class
        );

        log.info("[RecipeRecommend] Gemini response | userId: {}, recipeId: {}", userId, aiResult.recipeId());

        // 5. 검증 및 AiRecipeRecommendResponse 변환
        return toResponse(aiResult, candidates);
    }

    private Set<Long> ingredientIds(Long userId, UserFoodIngredientType type) {
        return userFoodIngredientRepository.findAllWithFoodIngredientByUserIdAndRelationType(userId, type)
                .stream()
                .map(userIngredient -> userIngredient.getFoodIngredient().getId())
                .collect(Collectors.toSet());
    }

    private String createPrompt(List<Candidate> candidates, SkillLevel skillLevel) {
        String candidateLines = candidates.stream()
                .map(c -> "- recipeId=" + c.recipeId
                        + ", menuId=" + c.menuId
                        + ", name=" + c.menuName
                        + ", ingredients=" + c.ingredientNames
                        + ", ownedIngredientCount=" + c.ownedIngredientCount
                        + ", totalIngredientCount=" + c.totalIngredientCount
                        + ", difficulty=" + c.difficultyLevel
                        + ", timeRequiredMinutes=" + c.timeRequired
                        + ", cookedBefore=" + c.cookedBefore)
                .collect(Collectors.joining("\n"));

        return """
                You MUST select EXACTLY ONE best recipe for the user from the candidate list below.
                
                User Skill Level: %s
                
                Selection Rules:
                1. Pick the single recipe that best balances the user's skill level and higher ownedIngredientCount.
                2. Prefer cookedBefore=false if available.
                3. Write a concise and friendly Korean reason for recommending this recipe.
                
                Candidates:
                %s
                """.formatted(skillLevel == null ? "BEGINNER" : skillLevel.name(), candidateLines);
    }

    private AiRecipeRecommendResponse toResponse(
            RecipeRecommendationGeminiResponse aiResult,
            List<Candidate> candidates
    ) {
        if (aiResult == null || aiResult.recipeId() == null) {
            throw new CustomException(MealPlanErrorCode.INVALID_AI_RECOMMENDATION);
        }

        Map<Long, Candidate> candidateMap = candidates.stream()
                .collect(Collectors.toMap(Candidate::recipeId, c -> c));

        Candidate selected = candidateMap.get(aiResult.recipeId());
        if (selected == null) {
            throw new CustomException(MealPlanErrorCode.INVALID_AI_RECOMMENDATION);
        }

        // AiRecipeRecommendResponse static factory 메서드 활용
        return AiRecipeRecommendResponse.from(
                selected.recipe().getMenu(),
                selected.recipeId(),
                aiResult.reason()
        );
    }

    private record Candidate(
            Recipe recipe,
            Long recipeId,
            Long menuId,
            String menuName,
            DifficultyLevel difficultyLevel,
            List<String> ingredientNames,
            int ownedIngredientCount,
            int totalIngredientCount,
            int difficultyScore,
            int timeRequired,
            boolean cookedBefore
    ) {
        static Candidate from(Recipe recipe, Set<Long> ownedIngredientIds, Set<Long> cookedMenuIds) {
            int ownedCount = (int) recipe.getRecipeFoodIngredients().stream()
                    .map(relation -> relation.getFoodIngredient().getId())
                    .filter(ownedIngredientIds::contains)
                    .count();

            List<String> ingredientNames = recipe.getRecipeFoodIngredients().stream()
                    .map(relation -> relation.getFoodIngredient().getName())
                    .toList();

            return new Candidate(
                    recipe,
                    recipe.getId(),
                    recipe.getMenu().getId(),
                    recipe.getMenu().getName(),
                    recipe.getMenu().getDifficultyLevel(),
                    ingredientNames,
                    ownedCount,
                    recipe.getRecipeFoodIngredients().size(),
                    recipe.getMenu().getDifficultyLevel().ordinal() + 1,
                    recipe.getMenu().getTimeRequired(),
                    cookedMenuIds.contains(recipe.getMenu().getId())
            );
        }
    }

    /**
     *
     * @param userId
     * @param request
     * @return
     */
    @Transactional(readOnly = true)
    public List<AiRecipeRecommendResponse> reRecommendRecipes(
            Long userId,
            RecipeReRecommendRequest request
    ) {
        // 1. 사용자 제외 식재료 및 보유 조리도구 수집
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        Set<Long> forbiddenIngredientIds = ingredientIds(userId, UserFoodIngredientType.EXCEPTION);
        Set<Long> ownedEquipmentIds = userCookingEquipmentRepository.findAllByUserId(userId).stream()
                .map(userEquipment -> userEquipment.getCookingEquipment().getId())
                .collect(Collectors.toSet());
        Set<Long> cookedMenuIds = new HashSet<>(planMenuRepository.findCompletedMenuIdsByUserId(userId));

        // 2. DB 동적 필터링을 통해 후보군 조회 (Safety Rule + Optional Filter)
        List<Recipe> filteredRecipes = recipeRepository.findCandidateRecipesByDbFilter(
                forbiddenIngredientIds,
                ownedEquipmentIds,
                request.difficultyLevel(),
                request.timeFilter(),
                request.desiredIngredientIds()
        );

        // 3. AI 프롬프트 전달용 Candidate DTO 구성
        // - previousRecipeId가 존재할 경우, 후보군에서 완벽히 제외하여 AI 오선택 방지
        Set<Long> ownedIngredientIds = ingredientIds(userId, UserFoodIngredientType.OWN);
        List<Candidate> candidates = filteredRecipes.stream()
                .filter(recipe -> request.previousRecipeId() == null
                        || !recipe.getId().equals(request.previousRecipeId()))
                .map(recipe -> Candidate.from(recipe, ownedIngredientIds, cookedMenuIds))
                .toList();

        if (candidates.size() < 3) {
            throw new CustomException(MealPlanErrorCode.NO_RECOMMENDABLE_RECIPE);
        }

        // 4. Gemini AI 호출 (3개 레시피 추천)
        RecipeReRecommendGeminiResponse aiResult = geminiUtil.callFunction(
                geminiProperties.menuAnalyzeModel(),
                createReRecommendPrompt(candidates, user.getSkillLevel(), request),
                RecipeReRecommendFunctionDeclaration.create(),
                RecipeReRecommendGeminiResponse.class
        );

        // 5. 결과 검증 및 AiRecipeRecommendResponse 리스트로 변환
        return toReRecommendResponse(aiResult, candidates);
    }

    private String createReRecommendPrompt(
            List<Candidate> candidates,
            SkillLevel skillLevel,
            RecipeReRecommendRequest request
    ) {
        String candidateLines = candidates.stream()
                .map(c -> "- recipeId=" + c.recipeId
                        + ", menuId=" + c.menuId
                        + ", name=" + c.menuName
                        + ", ingredients=" + c.ingredientNames
                        + ", ownedIngredientCount=" + c.ownedIngredientCount
                        + ", totalIngredientCount=" + c.totalIngredientCount
                        + ", difficulty=" + c.difficultyLevel
                        + ", timeRequiredMinutes=" + c.timeRequired
                        + ", cookedBefore=" + c.cookedBefore)
                .collect(Collectors.joining("\n"));

        return """
        You MUST select EXACTLY THREE DISTINCT recipes from the candidate list below.
        
        User Info:
        - Cooking Skill: %s
        - Applied Filters: Difficulty=%s, TimeFilter=%s, DesiredIngredients=%s
        
        Selection Rules:
        1. Choose 3 distinct recipes that best fit the candidate list and user filters.
        2. DIVERSITY RULE: The 3 selected recipes MUST have DIFFERENT culinary styles or cooking categories (e.g., mix different categories like soup/stew, stir-fry, main dish, rice/noodle dish, side dish) to give the user diverse choices.
        3. For each selected recipe, provide a compelling and natural Korean reason for the recommendation.
        
        Candidates:
        %s
        """.formatted(
                skillLevel == null ? "BEGINNER" : skillLevel.name(),
                request.difficultyLevel(),
                request.timeFilter(),
                request.desiredIngredientIds(),
                candidateLines
        );
    }

    private List<AiRecipeRecommendResponse> toReRecommendResponse(
            RecipeReRecommendGeminiResponse aiResult,
            List<Candidate> candidates
    ) {
        if (aiResult == null || aiResult.recipes() == null || aiResult.recipes().size() != 3) {
            throw new CustomException(MealPlanErrorCode.INVALID_AI_RECOMMENDATION);
        }

        Map<Long, Candidate> candidateMap = candidates.stream()
                .collect(Collectors.toMap(Candidate::recipeId, c -> c));

        return aiResult.recipes().stream()
                .map(rec -> {
                    Candidate candidate = candidateMap.get(rec.recipeId());
                    if (candidate == null) {
                        throw new CustomException(MealPlanErrorCode.INVALID_AI_RECOMMENDATION);
                    }
                    return AiRecipeRecommendResponse.from(
                            candidate.recipe().getMenu(),
                            candidate.recipeId(),
                            rec.reason()
                    );
                })
                .toList();
    }
}