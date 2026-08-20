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
import com.likelion.routineeatbe.domain.recipe.exception.RecipeErrorCode;
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
    private static final String RESPONSE_WRITING_RULES = """
            Response Writing Rules:
            - 쉽고 자연스러운 해요체를 쓴다.
            - 따뜻하되 유치하거나 과장되게 쓰지 않는다.
            - 한 문장에는 한 가지 행동만 담는다.
            - 실제 조리 순서에 맞춰 짧고 직접적으로 쓴다.
            - 사용자가 해야 할 행동을 문장 앞에 쓴다.
            - 시간, 온도, 불 세기, 수량, 크기는 제공된 값을 그대로 쓴다.
            - 제공되지 않은 수치나 조리 정보를 추측하지 않는다.
            - '적당히', '조금', '먹기 좋게', '알맞게'처럼 기준이 모호한 표현을 쓰지 않는다.
            - 익은 정도나 완성 상태는 눈으로 확인할 수 있는 표현으로 쓴다.
            - 예: '노릇하게 익혀요'보다 '아랫면이 연한 갈색이 될 때까지 익혀요'라고 쓴다.
            - 전문적인 조리 용어는 쉬운 말로 바꾼다.
            - 전문 용어가 꼭 필요하면 바로 뒤에서 짧게 설명한다.
            - 같은 행동이나 정보를 반복하지 않는다.
            - 재료명과 도구명은 등록된 명칭을 그대로 쓴다.
            - 서로 다른 행동을 한 문장에 묶지 않는다.
            - 주의사항은 위험 요소와 피해야 할 행동을 명확하게 쓴다.
            - '누구나', '무조건', '완벽하게', '실패 없이'를 쓰지 않는다.
            - '간단해요', '쉬워요'처럼 근거 없는 평가만 쓰지 않는다.
            - 건강, 피부, 체중 변화나 효과를 단정하지 않는다.
            """;

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
                1. [CRITICAL RULE] You MUST STRICTLY prioritize the recipe with the HIGHEST `ownedIngredientCount`. This is your absolute first priority.
                2. Balance the user's skill level only among the top recipes with the most owned ingredients.
                3. Prefer cookedBefore=false if available.
                4. Write a concise and friendly Korean reason for recommending this recipe, mentioning the ingredients the user already has.

                %s
                
                Candidates:
                %s
                """.formatted(
                skillLevel == null ? "BEGINNER" : skillLevel.name(),
                RESPONSE_WRITING_RULES,
                candidateLines
        );
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
        log.info("[RecipeReRecommend] 재추천 시작 | userId: {}, previousRecipeId: {}",
                userId, request.previousRecipeId());

        // ==========================================
        // 1. DB 조회 및 후보군 생성
        // ==========================================
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        Set<Long> forbiddenIngredientIds = ingredientIds(userId, UserFoodIngredientType.EXCEPTION);
        Set<Long> ownedEquipmentIds = userCookingEquipmentRepository.findAllByUserId(userId).stream()
                .map(userEquipment -> userEquipment.getCookingEquipment().getId())
                .collect(Collectors.toSet());
        Set<Long> cookedMenuIds = new HashSet<>(planMenuRepository.findCompletedMenuIdsByUserId(userId));

        List<Recipe> filteredRecipes = recipeRepository.findCandidateRecipesByDbFilter(
                forbiddenIngredientIds,
                ownedEquipmentIds,
                request.difficultyLevel(),
                request.timeFilter(),
                request.desiredIngredientIds()
        );

        Set<Long> ownedIngredientIds = ingredientIds(userId, UserFoodIngredientType.OWN);

        // previousRecipeId가 존재할 경우 후보군에서 미리 완전히 제외
        List<Candidate> candidates = filteredRecipes.stream()
                .filter(recipe -> request.previousRecipeId() == null
                        || !recipe.getId().equals(request.previousRecipeId()))
                .map(recipe -> Candidate.from(recipe, ownedIngredientIds, cookedMenuIds))
                .toList();

        // 추천 가능한 후보군이 3개 미만이면 즉시 예외 반환
        if (candidates.size() < 3) {
            log.warn("[RecipeReRecommend] 후보군 부족으로 추천 불가 | candidateSize: {}", candidates.size());
            throw new CustomException(MealPlanErrorCode.NO_RECOMMENDABLE_RECIPE);
        }

        // ==========================================
        // 2. Gemini AI 호출 및 검증 (최대 5회 재시도 루프)
        // ==========================================
        int maxTries = 5;
        int currentTry = 0;

        while (currentTry < maxTries) {
            currentTry++;
            log.info("[RecipeReRecommend] AI 호출 시도: {}/{}", currentTry, maxTries);

            try {
                // Gemini AI 호출
                RecipeReRecommendGeminiResponse aiResult = geminiUtil.callFunction(
                        geminiProperties.menuAnalyzeModel(),
                        createReRecommendPrompt(candidates, user.getSkillLevel(), request),
                        RecipeReRecommendFunctionDeclaration.create(),
                        RecipeReRecommendGeminiResponse.class
                );

                // AI 결과 검증 및 DTO 변환 (내부에서 검증 실패 시 CustomException 발생)
                List<AiRecipeRecommendResponse> responseList = toReRecommendResponse(aiResult, candidates);

                log.info("[RecipeReRecommend] 재추천 성공 | 추천 개수: {}", responseList.size());
                return responseList;

            } catch (Exception e) {
                log.warn("[RecipeReRecommend] AI 재추천 시도 실패 ({} / {}): {}", currentTry, maxTries, e.getMessage());
            }
        }

        //  5회 연속 실패 시 예외를 던지지 않고 자바 자체 알고리즘으로 폴백하여 응답 보장
        log.warn("[RecipeReRecommend] AI 호출 5회 실패로 인해 기본 알고리즘 폴백 추천을 진행합니다.");
        return getFallbackRecommendations(candidates);
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
        
        CRITICAL RULES:
        1. Select recipeId STRICTLY from the provided Candidates list. NEVER invent or hallucinate new IDs.
        2. Even if ownedIngredientCount is 0 for all candidates, ALWAYS select 3 recipes based on difficulty match, time required, and category diversity.
        3. DIVERSITY RULE: Choose 3 recipes with DIFFERENT culinary styles/dish types (e.g., main dish, soup/stew, stir-fry, side dish).
        4. Provide a friendly Korean reason for each recommendation.
        
        User Info:
        - Cooking Skill: %s
        - Applied Filters: Difficulty=%s, TimeFilter=%s, DesiredIngredients=%s
        
        Selection Rules:
        1. Choose 3 distinct recipes that best fit the candidate list and user filters.
        2. DIVERSITY RULE: The 3 selected recipes MUST have DIFFERENT culinary styles or cooking categories (e.g., mix different categories like soup/stew, stir-fry, main dish, rice/noodle dish, side dish) to give the user diverse choices.
        3. For each selected recipe, provide a compelling and natural Korean reason for the recommendation.

        %s
        
        Candidates:
        %s
        """.formatted(
                skillLevel == null ? "BEGINNER" : skillLevel.name(),
                request.difficultyLevel(),
                request.timeFilter(),
                request.desiredIngredientIds(),
                RESPONSE_WRITING_RULES,
                candidateLines
        );
    }

    // [개선 3] AI 연속 실패 시 안전하게 상위 3개 레시피를 반환하는 폴백 메서드
    private List<AiRecipeRecommendResponse> getFallbackRecommendations(List<Candidate> candidates) {
        return candidates.stream()
                .limit(3)
                .map(c -> AiRecipeRecommendResponse.from(
                        c.recipe().getMenu(),
                        c.recipeId(),
                        "취향과 조리 난이도를 고려하여 추천하는 대표 레시피입니다."
                ))
                .toList();
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