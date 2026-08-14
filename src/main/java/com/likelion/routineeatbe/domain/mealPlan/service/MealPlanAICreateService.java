package com.likelion.routineeatbe.domain.mealPlan.service;

import com.likelion.routineeatbe.domain.mealPlan.dto.gemini.MealRecommendationFunctionDeclaration;
import com.likelion.routineeatbe.domain.mealPlan.dto.gemini.MealRecommendationGeminiResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.AiMealRecommendationResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import com.likelion.routineeatbe.domain.mealPlan.exception.MealPlanErrorCode;
import com.likelion.routineeatbe.domain.mealPlan.repository.PlanMenuRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.entity.RecipeCookingEquipment;
import com.likelion.routineeatbe.domain.recipeCookingEquipment.repository.RecipeCookingEquipmentRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
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

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealPlanAICreateService {

    // ==========================================
    // 0. 상수 정의 (식단 구성 규칙)
    // ==========================================
    private static final int MENUS_PER_PLAN = 3; // 하나의 식단(Plan)당 들어갈 메뉴 개수 (3개)
    private static final int PLAN_COUNT = MealPlanType.values().length; // 식단 종류 개수 (PRACTICE, USEALL, SIMPLE, RECYCLING -> 총 4개)
    private static final int REQUIRED_MENU_COUNT = MENUS_PER_PLAN * PLAN_COUNT; // 4개 식단을 모두 만들 때 필요한 총 메뉴 수 (3 * 4 = 12개)
    private static final int NON_USE_ALL_REQUIRED_MENU_COUNT = MENUS_PER_PLAN * (PLAN_COUNT - 1); // USEALL 제외 시 필요한 총 메뉴 수 (3 * 3 = 9개)
    private static final int MAX_AI_CANDIDATES = 60; // AI 프롬프트(토큰 제약)로 넘길 최대 후보 레시피 개수

    private final UserRepository userRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final UserCookingEquipmentRepository userCookingEquipmentRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeCookingEquipmentRepository recipeCookingEquipmentRepository;
    private final PlanMenuRepository planMenuRepository;
    private final GeminiUtil geminiUtil;
    private final GeminiProperties geminiProperties;

    /**
     * 메인 비즈니스 로직: 사용자 맞춤형 식단을 추천합니다.
     */
    @Transactional(readOnly = true)
    public AiMealRecommendationResponse recommendThreeMeals(Long userId) {

        // ----------------------------------------------------
        // [STEP 1] 사용자 기본 정보 및 개인화 제약 조건(필터링 기준) 조회
        // ----------------------------------------------------
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        // 1-1. 절대 먹으면 안 되는 제외(EXCEPTION) 식재료 ID 수집
        Set<Long> forbiddenIngredientIds = ingredientIds(userId, UserFoodIngredientType.EXCEPTION);

        // 1-2. 현재 사용자가 보유(OWN)하고 있는 식재료 ID 수집
        Set<Long> ownedIngredientIds = ingredientIds(userId, UserFoodIngredientType.OWN);

        // 1-3. 현재 사용자가 보유하고 있는 조리 도구 ID 수집
        Set<Long> ownedEquipmentIds = userCookingEquipmentRepository.findAllByUserId(userId).stream()
                .map(userEquipment -> userEquipment.getCookingEquipment().getId())
                .collect(Collectors.toSet());

        // 1-4. 사용자가 이전에 해먹은 적 있는 메뉴 ID 수집 (중복 추천 방지 우선순위 반영용)
        Set<Long> cookedMenuIds = new HashSet<>(planMenuRepository.findCompletedMenuIdsByUserId(userId));

        // 1-5. DB 전체 기본 레시피 및 각 레시피별 필요 조리도구 맵핑
        List<Recipe> recipes = recipeRepository.findAllBasicWithMenuAndFoodIngredients();
        Map<Long, Set<Long>> requiredEquipmentIds = requiredEquipmentIds(recipes);

        // ----------------------------------------------------
        // [STEP 2] 백엔드 하드 필터링(Safety Rule) 및 후보군 정렬
        // ----------------------------------------------------
        List<Candidate> candidates = recipes.stream()
                // [안전 규칙 1] 제외(EXCEPTION) 식재료가 1개라도 포함된 레시피는 아예 제외
                .filter(recipe -> recipe.getRecipeFoodIngredients().stream()
                        .map(relation -> relation.getFoodIngredient().getId())
                        .noneMatch(forbiddenIngredientIds::contains))

                // [안전 규칙 2] 레시피에 필요한 필수 조리도구를 사용자가 모두 가지지 않았다면 제외
                .filter(recipe -> ownedEquipmentIds.containsAll(
                        requiredEquipmentIds.getOrDefault(recipe.getId(), Set.of())))

                // 도메인 객체(Recipe)를 평가용 내부 DTO(Candidate)로 변환
                .map(recipe -> Candidate.from(recipe, ownedIngredientIds, cookedMenuIds))

                // [우선순위 정렬]
                // 1) 보유 재료 많은 순 -> 2) 안 해본 요리 우선 -> 3) 난이도 쉬운 순 -> 4) 조리시간 짧은 순 -> 5) ID 순
                .sorted(Comparator.comparingInt(Candidate::ownedIngredientCount).reversed()
                        .thenComparing(Candidate::cookedBefore)
                        .thenComparingInt(Candidate::difficultyScore)
                        .thenComparingInt(Candidate::timeRequired)
                        .thenComparing(Candidate::menuId))

                // AI 토큰 및 연산 효율화를 위해 상위 최대 60개만 추출
                .limit(MAX_AI_CANDIDATES)
                .toList();

        // ----------------------------------------------------
        // [STEP 3] 냉장고 재료 먹기(USEALL) 플랜 생성 가능 여부 판별
        // ----------------------------------------------------
        // 후보 중 "모든 재료를 사용자가 가지고 있는" 완벽한 냉파 메뉴 ID만 추출
        Set<Long> useAllCandidateMenuIds = candidates.stream()
                .filter(Candidate::usesOnlyOwnedIngredients)
                .map(Candidate::menuId)
                .collect(Collectors.toSet());

        log.info(
                "[MealPlanRecommendation] candidate summary | userId: {}, safeCandidateCount: {}, useAllCandidateCount: {}, ownedIngredientCount: {}, ownedEquipmentCount: {}",
                userId, candidates.size(), useAllCandidateMenuIds.size(), ownedIngredientIds.size(), ownedEquipmentIds.size()
        );

        // 냉털(USEALL) 플랜용 메뉴가 3개 이상이면 USEALL 플랜 생성 가능으로 판단
        boolean useAllAvailable = useAllCandidateMenuIds.size() >= MENUS_PER_PLAN;

        // USEALL 가능 여부에 따라 필요한 총 후보 레시피 최소 수량 결정 (12개 또는 9개)
        int requiredCandidateCount = useAllAvailable
                ? REQUIRED_MENU_COUNT
                : NON_USE_ALL_REQUIRED_MENU_COUNT;

        // [핵심 예외 발생 지점] 안전 후보군 개수가 최소 필요 수량보다 적으면 에러 발생!
        if (candidates.size() < requiredCandidateCount) {
            throw new CustomException(MealPlanErrorCode.NO_RECOMMENDABLE_RECIPE);
        }

        if (!useAllAvailable) {
            log.info(
                    "[MealPlanRecommendation] USEALL unavailable | userId: {}, reason: fewer than {} menus can be made using only owned ingredients",
                    userId, MENUS_PER_PLAN
            );
        }

        // ----------------------------------------------------
        // [STEP 4] Gemini AI 호출 (프롬프트 구성 및 Function Calling)
        // ----------------------------------------------------
        MealRecommendationGeminiResponse aiResult = geminiUtil.callFunction(
                geminiProperties.menuAnalyzeModel(),
                createPrompt(candidates, useAllCandidateMenuIds, user.getSkillLevel(), useAllAvailable),
                MealRecommendationFunctionDeclaration.create(useAllAvailable),
                MealRecommendationGeminiResponse.class
        );
        log.info("[MealPlanRecommendation] Gemini response | userId: {}, response: {}", userId, aiResult);

        // ----------------------------------------------------
        // [STEP 5] AI 응답 검증 및 최종 DTO 반환
        // ----------------------------------------------------
        return toResponse(aiResult, candidates, useAllCandidateMenuIds, useAllAvailable);
    }

    /**
     * [보조 메서드] 특정 릴레이션 타입(EXCEPTION, OWN, RESERVATION)에 해당하는 식재료 ID Set을 가져옵니다.
     */
    private Set<Long> ingredientIds(Long userId, UserFoodIngredientType type) {
        return userFoodIngredientRepository.findAllWithFoodIngredientByUserIdAndRelationType(userId, type)
                .stream()
                .map(userIngredient -> userIngredient.getFoodIngredient().getId())
                .collect(Collectors.toSet());
    }

    /**
     * [보조 메서드] 레시피 목록을 받아 { recipeId -> 필수 조리도구 ID Set } 형태의 Map으로 반환합니다.
     */
    private Map<Long, Set<Long>> requiredEquipmentIds(List<Recipe> recipes) {
        List<Long> recipeIds = recipes.stream().map(Recipe::getId).toList();
        if (recipeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Set<Long>> result = new HashMap<>();
        for (RecipeCookingEquipment relation : recipeCookingEquipmentRepository
                .findAllByRecipeIdInWithCookingEquipment(recipeIds)) {
            result.computeIfAbsent(relation.getRecipe().getId(), ignored -> new HashSet<>())
                    .add(relation.getCookingEquipment().getId());
        }
        return result;
    }

    /**
     * [보조 메서드] Gemini AI에게 전달할 프롬프트(지시문) 텍스트를 생성합니다.
     */
    private String createPrompt(
            List<Candidate> candidates,
            Set<Long> useAllCandidateMenuIds,
            SkillLevel skillLevel,
            boolean useAllAvailable
    ) {
        // AI가 메인 재료를 파악할 수 있도록 ingredients 항목 추가
        String candidateLines = candidates.stream()
                .map(candidate -> "- menuId=" + candidate.menuId
                        + ", name=" + candidate.menuName
                        + ", ingredients=" + candidate.ingredientNames
                        + ", ownedIngredientCount=" + candidate.ownedIngredientCount
                        + ", totalIngredientCount=" + candidate.totalIngredientCount
                        + ", difficulty=" + candidate.difficultyScore
                        + ", timeRequiredMinutes=" + candidate.timeRequired
                        + ", cookedBefore=" + candidate.cookedBefore)
                .collect(Collectors.joining("\n"));

        // USEALL 플랜 생성 가능 여부에 따른 AI 지시사항 분기 처리
        String requiredPlanTypes = useAllAvailable ? "PRACTICE, USEALL, SIMPLE, RECYCLING" : "PRACTICE, SIMPLE, RECYCLING";
        String useAllInstruction = useAllAvailable
                ? "- USEALL: choose ONLY from these menu IDs: " + useAllCandidateMenuIds.stream().sorted().toList()
                + ". These are the only menus whose every required ingredient is currently owned. Do not select any other ID for USEALL."
                : "- USEALL: do not return this plan because fewer than three menus can be made using only owned ingredients.";

        return """
                You MUST return exactly these plans: %s. Return each type exactly once.
                Every plan MUST contain exactly three DISTINCT menu IDs.
                Every plan MUST contain exactly three DISTINCT menu IDs.
                Across all returned plans, AT MOST ONE menu ID can be reused (e.g., up to 1 shared menu ID total across plans).
                Prefer completely distinct menu IDs if possible.
                Use only the listed candidate menu IDs.
                All candidates already passed excluded-ingredient and required-cooking-equipment checks.

                Plan objectives:
                - PRACTICE: improve the user's cooking skill. The user's current skill is %s; prefer appropriately challenging difficulty.
                %s
                - SIMPLE: prefer lower difficulty and shorter timeRequiredMinutes.
                - RECYCLING: choose three distinct menus that share the SAME primary/main food ingredient (e.g., salmon, chicken, pork, beef, tofu, egg, etc.). Do NOT count basic condiments or seasonings (e.g., soy sauce, salt, garlic, sugar, cooking oil) as the main ingredient.
                Prefer menus with cookedBefore=false when the objective scores are comparable.
                Candidates:
                %s
                """.formatted(
                requiredPlanTypes,
                skillLevel == null ? "BEGINNER" : skillLevel.name(),
                useAllInstruction,
                candidateLines
        );
    }

    /**
     * [보조 메서드] AI가 반환한 결과가 백엔드의 비즈니스 규칙을 준수했는지 엄격히 검증합니다.
     */
    private AiMealRecommendationResponse toResponse(
            MealRecommendationGeminiResponse aiResult,
            List<Candidate> candidates,
            Set<Long> useAllCandidateMenuIds,
            boolean useAllAvailable
    ) {
        int expectedPlanCount = useAllAvailable ? PLAN_COUNT : PLAN_COUNT - 1;

        // 1. AI 응답 자체가 없거나 플랜 개수가 안 맞으면 예외
        if (aiResult == null || aiResult.plans() == null || aiResult.plans().size() != expectedPlanCount) {
            throw invalidAiRecommendation("plans must contain exactly " + expectedPlanCount + " items", aiResult);
        }

        Map<Long, Candidate> candidatesByMenuId = candidates.stream()
                .collect(Collectors.toMap(Candidate::menuId, candidate -> candidate));
        Set<MealPlanType> planTypes = new HashSet<>();

        // [변경 1] Set 대신 전체 선택된 menuId를 담을 List 사용
        List<Long> allSelectedMenuIds = new ArrayList<>();

        // 2. 각 플랜 및 메뉴 항목을 순회하며 검증 진행
        List<AiMealRecommendationResponse.Plan> plans = aiResult.plans().stream().map(aiPlan -> {
            MealPlanType type = parsePlanType(aiPlan.type());

            // USEALL이 불가능한데 AI가 USEALL을 보냈거나, 플랜 타입 중복, 메뉴가 3개가 아닌 경우 체크
            if ((!useAllAvailable && type == MealPlanType.USEALL)
                    || !planTypes.add(type) || aiPlan.menus() == null || aiPlan.menus().size() != MENUS_PER_PLAN) {
                throw invalidAiRecommendation("duplicate plan type or a plan does not contain exactly three menus", aiResult);
            }

            // [변경 2] 단일 플랜 내에서 메뉴 3개가 서로 중복되는지 검증 (한 플랜 안에는 서로 다른 메뉴 3개여야 함)
            Set<Long> inPlanMenuIds = aiPlan.menus().stream()
                    .map(MealRecommendationGeminiResponse.Menu::menuId)
                    .collect(Collectors.toSet());
            if (inPlanMenuIds.size() != MENUS_PER_PLAN) {
                throw invalidAiRecommendation("a single plan contains duplicate menu IDs within itself", aiResult);
            }

            List<AiMealRecommendationResponse.Menu> menus = aiPlan.menus().stream().map(aiMenu -> {
                Candidate candidate = candidatesByMenuId.get(aiMenu.menuId());
                // AI가 후보군에 없던 menuId를 생성했는지 체크
                if (candidate == null) {
                    throw invalidAiRecommendation("menuId " + aiMenu.menuId() + " is not in the safe candidate list", aiResult);
                }
                // USEALL 플랜인데 보유 식재료 전용 메뉴가 아닌 항목을 골랐는지 체크
                if (type == MealPlanType.USEALL && !useAllCandidateMenuIds.contains(aiMenu.menuId())) {
                    throw invalidAiRecommendation("USEALL menuId " + aiMenu.menuId() + " requires an ingredient the user does not own", aiResult);
                }

                allSelectedMenuIds.add(aiMenu.menuId()); // 전체 선택된 메뉴 리스트에 추가
                return new AiMealRecommendationResponse.Menu(
                        candidate.menuId, candidate.menuName, aiMenu.reason());
            }).toList();
            return new AiMealRecommendationResponse.Plan(type, aiPlan.reason(), menus);
        }).toList();

        // 3. 최종 요구사항(플랜 종류) 검증
        Set<MealPlanType> expectedPlanTypes = useAllAvailable
                ? Set.of(MealPlanType.PRACTICE, MealPlanType.USEALL, MealPlanType.SIMPLE, MealPlanType.RECYCLING)
                : Set.of(MealPlanType.PRACTICE, MealPlanType.SIMPLE, MealPlanType.RECYCLING);

        if (!planTypes.equals(expectedPlanTypes)) {
            throw invalidAiRecommendation("required plan types do not match", aiResult);
        }

        // [변경 3] 전체 플랜 간 메뉴 중복 개수 검증 (최대 1개 중복만 허용)
        int totalMenuSlots = expectedPlanCount * MENUS_PER_PLAN; // 총 메뉴 칸 수 (예: 12개 또는 9개)
        long distinctMenuCount = allSelectedMenuIds.stream().distinct().count(); // 중복을 제거한 실제 고유 메뉴 수
        int overlapCount = totalMenuSlots - (int) distinctMenuCount; // 겹친 횟수

        // overlapCount = 0 (중복 없음), overlapCount = 1 (1개 메뉴가 2개 플랜에 중복 사용됨)
        // 겹친 횟수가 2 이상(2개 이상 중복)이면 에러
        if (overlapCount > 1) {
            throw invalidAiRecommendation("more than 1 menu overlap found across plans (overlapCount: " + overlapCount + ")", aiResult);
        }

        Map<MealPlanType, AiMealRecommendationResponse.Plan> plansByType = plans.stream()
                .collect(Collectors.toMap(AiMealRecommendationResponse.Plan::type, plan -> plan));

        return new AiMealRecommendationResponse(
                plansByType.get(MealPlanType.PRACTICE),
                plansByType.get(MealPlanType.USEALL),
                plansByType.get(MealPlanType.SIMPLE),
                plansByType.get(MealPlanType.RECYCLING)
        );
    }

    private MealPlanType parsePlanType(String type) {
        try {
            return MealPlanType.valueOf(type);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw invalidAiRecommendation("unknown meal plan type: " + type, null);
        }
    }

    private CustomException invalidAiRecommendation(String reason, MealRecommendationGeminiResponse aiResult) {
        log.warn("[MealPlanRecommendation] Invalid Gemini response | reason: {}, response: {}", reason, aiResult);
        return new CustomException(MealPlanErrorCode.INVALID_AI_RECOMMENDATION);
    }

    /**
     * [내부 Record 클래스] AI 추천 판단을 돕기 위해 레시피의 핵심 정보만 요약한 데이터 객체입니다.
     */
    private record Candidate(Long menuId, String menuName, List<String> ingredientNames, int ownedIngredientCount,
                             int totalIngredientCount, int difficultyScore, int timeRequired,
                             boolean cookedBefore) {
        static Candidate from(Recipe recipe, Set<Long> ownedIngredientIds, Set<Long> cookedMenuIds) {
            int ownedCount = (int) recipe.getRecipeFoodIngredients().stream()
                    .map(relation -> relation.getFoodIngredient().getId())
                    .filter(ownedIngredientIds::contains)
                    .count();
            List<String> ingredientNames = recipe.getRecipeFoodIngredients().stream()
                    .map(relation -> relation.getFoodIngredient().getName())
                    .toList();
            return new Candidate(
                    recipe.getMenu().getId(),
                    recipe.getMenu().getName(),
                    ingredientNames,
                    ownedCount,
                    recipe.getRecipeFoodIngredients().size(),
                    recipe.getMenu().getDifficultyLevel().ordinal() + 1,
                    recipe.getMenu().getTimeRequired(),
                    cookedMenuIds.contains(recipe.getMenu().getId())
            );
        }

        // 해당 레시피의 '모든' 식재료를 사용자가 가지고 있는지 확인하는 메서드
        boolean usesOnlyOwnedIngredients() {
            return totalIngredientCount > 0 && ownedIngredientCount == totalIngredientCount;
        }
    }
}