package com.likelion.routineeatbe.domain.mealPlan.service;

import com.likelion.routineeatbe.domain.mealPlan.dto.gemini.MealRecommendationFunctionDeclaration;
import com.likelion.routineeatbe.domain.mealPlan.dto.gemini.MealRecommendationGeminiResponse;
import com.likelion.routineeatbe.domain.mealPlan.dto.response.AiMealRecommendationResponse;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlanType;
import com.likelion.routineeatbe.domain.mealPlan.exception.MealPlanErrorCode;
import com.likelion.routineeatbe.domain.mealPlan.repository.PlanMenuRepository;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
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

    private static final int MENUS_PER_PLAN = 3;
    private static final int PLAN_COUNT = MealPlanType.values().length;
    private static final int REQUIRED_MENU_COUNT = MENUS_PER_PLAN * PLAN_COUNT;
    private static final int NON_USE_ALL_REQUIRED_MENU_COUNT = MENUS_PER_PLAN * (PLAN_COUNT - 1);
    private static final int MAX_AI_CANDIDATES = 60;

    private final UserRepository userRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final UserCookingEquipmentRepository userCookingEquipmentRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeCookingEquipmentRepository recipeCookingEquipmentRepository;
    private final PlanMenuRepository planMenuRepository;
    private final GeminiUtil geminiUtil;
    private final GeminiProperties geminiProperties;

    @Transactional(readOnly = true)
    public AiMealRecommendationResponse recommendThreeMeals(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        Set<Long> forbiddenIngredientIds = ingredientIds(userId, UserFoodIngredientType.EXCEPTION);
        Set<Long> ownedIngredientIds = ingredientIds(userId, UserFoodIngredientType.OWN);

        Set<Long> ownedEquipmentIds = userCookingEquipmentRepository.findAllByUserId(userId).stream()
                .map(userEquipment -> userEquipment.getCookingEquipment().getId())
                .collect(Collectors.toSet());

        Set<Long> cookedMenuIds = new HashSet<>(planMenuRepository.findCompletedMenuIdsByUserId(userId));

        List<Recipe> recipes = recipeRepository.findAllBasicWithMenuAndFoodIngredients();
        Map<Long, Set<Long>> requiredEquipmentIds = requiredEquipmentIds(recipes);

        List<Candidate> candidates = recipes.stream()
                .filter(recipe -> recipe.getRecipeFoodIngredients().stream()
                        .map(relation -> relation.getFoodIngredient().getId())
                        .noneMatch(forbiddenIngredientIds::contains))
                .filter(recipe -> ownedEquipmentIds.containsAll(
                        requiredEquipmentIds.getOrDefault(recipe.getId(), Set.of())))
                .map(recipe -> Candidate.from(recipe, ownedIngredientIds, cookedMenuIds))
                .sorted(Comparator.comparingInt(Candidate::ownedIngredientCount).reversed()
                        .thenComparing(Candidate::cookedBefore)
                        .thenComparingInt(Candidate::difficultyScore)
                        .thenComparingInt(Candidate::timeRequired)
                        .thenComparing(Candidate::menuId))
                .limit(MAX_AI_CANDIDATES)
                .toList();

        Set<Long> useAllCandidateMenuIds = candidates.stream()
                .filter(Candidate::usesOnlyOwnedIngredients)
                .map(Candidate::menuId)
                .collect(Collectors.toSet());

        log.info(
                "[MealPlanRecommendation] candidate summary | userId: {}, safeCandidateCount: {}, useAllCandidateCount: {}, ownedIngredientCount: {}, ownedEquipmentCount: {}",
                userId, candidates.size(), useAllCandidateMenuIds.size(), ownedIngredientIds.size(), ownedEquipmentIds.size()
        );

        boolean useAllAvailable = useAllCandidateMenuIds.size() >= MENUS_PER_PLAN;
        int requiredCandidateCount = useAllAvailable ? REQUIRED_MENU_COUNT : NON_USE_ALL_REQUIRED_MENU_COUNT;

        if (candidates.size() < requiredCandidateCount) {
            throw new CustomException(MealPlanErrorCode.NO_RECOMMENDABLE_RECIPE);
        }

        if (!useAllAvailable) {
            log.info(
                    "[MealPlanRecommendation] USEALL unavailable | userId: {}, reason: fewer than {} menus can be made using only owned ingredients",
                    userId, MENUS_PER_PLAN
            );
        }

        MealRecommendationGeminiResponse aiResult = geminiUtil.callFunction(
                geminiProperties.menuAnalyzeModel(),
                createPrompt(candidates, useAllCandidateMenuIds, user.getSkillLevel(), useAllAvailable),
                MealRecommendationFunctionDeclaration.create(useAllAvailable),
                MealRecommendationGeminiResponse.class
        );
        log.info("[MealPlanRecommendation] Gemini response | userId: {}, response: {}", userId, aiResult);

        return toResponse(aiResult, candidates, useAllCandidateMenuIds, useAllAvailable);
    }

    private Set<Long> ingredientIds(Long userId, UserFoodIngredientType type) {
        return userFoodIngredientRepository.findAllWithFoodIngredientByUserIdAndRelationType(userId, type)
                .stream()
                .map(userIngredient -> userIngredient.getFoodIngredient().getId())
                .collect(Collectors.toSet());
    }

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

    private String createPrompt(
            List<Candidate> candidates,
            Set<Long> useAllCandidateMenuIds,
            SkillLevel skillLevel,
            boolean useAllAvailable
    ) {
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

    private AiMealRecommendationResponse toResponse(
            MealRecommendationGeminiResponse aiResult,
            List<Candidate> candidates,
            Set<Long> useAllCandidateMenuIds,
            boolean useAllAvailable
    ) {
        int expectedPlanCount = useAllAvailable ? PLAN_COUNT : PLAN_COUNT - 1;

        if (aiResult == null || aiResult.plans() == null || aiResult.plans().size() != expectedPlanCount) {
            throw invalidAiRecommendation("plans must contain exactly " + expectedPlanCount + " items", aiResult);
        }

        Map<Long, Candidate> candidatesByMenuId = candidates.stream()
                .collect(Collectors.toMap(Candidate::menuId, candidate -> candidate));
        Set<MealPlanType> planTypes = new HashSet<>();
        List<Long> allSelectedMenuIds = new ArrayList<>();

        List<AiMealRecommendationResponse.Plan> plans = aiResult.plans().stream().map(aiPlan -> {
            MealPlanType type = parsePlanType(aiPlan.type());

            if ((!useAllAvailable && type == MealPlanType.USEALL)
                    || !planTypes.add(type) || aiPlan.menus() == null || aiPlan.menus().size() != MENUS_PER_PLAN) {
                throw invalidAiRecommendation("duplicate plan type or a plan does not contain exactly three menus", aiResult);
            }

            Set<Long> inPlanMenuIds = aiPlan.menus().stream()
                    .map(MealRecommendationGeminiResponse.Menu::menuId)
                    .collect(Collectors.toSet());
            if (inPlanMenuIds.size() != MENUS_PER_PLAN) {
                throw invalidAiRecommendation("a single plan contains duplicate menu IDs within itself", aiResult);
            }

            List<AiMealRecommendationResponse.Menu> menus = aiPlan.menus().stream().map(aiMenu -> {
                Candidate candidate = candidatesByMenuId.get(aiMenu.menuId());

                if (candidate == null) {
                    throw invalidAiRecommendation("menuId " + aiMenu.menuId() + " is not in the safe candidate list", aiResult);
                }

                if (type == MealPlanType.USEALL && !useAllCandidateMenuIds.contains(aiMenu.menuId())) {
                    throw invalidAiRecommendation("USEALL menuId " + aiMenu.menuId() + " requires an ingredient the user does not own", aiResult);
                }

                allSelectedMenuIds.add(aiMenu.menuId());

                // sameRate와 price 적용
                return new AiMealRecommendationResponse.Menu(
                        candidate.menuId,
                        candidate.menuName,
                        candidate.difficultyLevel,
                        candidate.timeRequired,
                        candidate.sameRate,
                        candidate.price
                );
            }).toList();
            return new AiMealRecommendationResponse.Plan(type, aiPlan.reason(), menus);
        }).toList();

        Set<MealPlanType> expectedPlanTypes = useAllAvailable
                ? Set.of(MealPlanType.PRACTICE, MealPlanType.USEALL, MealPlanType.SIMPLE, MealPlanType.RECYCLING)
                : Set.of(MealPlanType.PRACTICE, MealPlanType.SIMPLE, MealPlanType.RECYCLING);

        if (!planTypes.equals(expectedPlanTypes)) {
            throw invalidAiRecommendation("required plan types do not match", aiResult);
        }

        int totalMenuSlots = expectedPlanCount * MENUS_PER_PLAN;
        long distinctMenuCount = allSelectedMenuIds.stream().distinct().count();
        int overlapCount = totalMenuSlots - (int) distinctMenuCount;

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
     * [내부 Record] Candidate에 sameRate와 price 필드 및 계산 로직 추가
     */
    private record Candidate(Long menuId,
                             String menuName,
                             DifficultyLevel difficultyLevel,
                             List<String> ingredientNames,
                             int ownedIngredientCount,
                             int totalIngredientCount,
                             int difficultyScore,
                             int timeRequired,
                             boolean cookedBefore,
                             Double sameRate,
                             Long price) {
        static Candidate from(Recipe recipe, Set<Long> ownedIngredientIds, Set<Long> cookedMenuIds) {
            int totalCount = recipe.getRecipeFoodIngredients().size();

            int ownedCount = (int) recipe.getRecipeFoodIngredients().stream()
                    .map(relation -> relation.getFoodIngredient().getId())
                    .filter(ownedIngredientIds::contains)
                    .count();

            // 1. 재료 일치율(sameRate) 계산 (Double)
            double sameRate = totalCount == 0 ? 0.0 : Math.round(((double) ownedCount / totalCount) * 100);

            // 2. 예상 재료 가격(price) 계산 (Long)
            // Math.round()의 반환 타입이 long이므로 (int) 캐스팅을 제거합니다.
            long totalPrice = Math.round(
                    recipe.getRecipeFoodIngredients().stream()
                            .mapToDouble(relation -> {
                                Long pricePerHundred = relation.getFoodIngredient().getPricePerHundred();
                                Double capacity = relation.getPrimaryNeedAmountValue();

                                if (pricePerHundred == null || capacity == null) {
                                    return 0.0;
                                }
                                return (capacity / 100.0) * pricePerHundred;
                            })
                            .sum()
            );

            List<String> ingredientNames = recipe.getRecipeFoodIngredients().stream()
                    .map(relation -> relation.getFoodIngredient().getName())
                    .toList();

            return new Candidate(
                    recipe.getMenu().getId(),
                    recipe.getMenu().getName(),
                    recipe.getMenu().getDifficultyLevel(),
                    ingredientNames,
                    ownedCount,
                    totalCount,
                    recipe.getMenu().getDifficultyLevel().ordinal() + 1,
                    recipe.getMenu().getTimeRequired(),
                    cookedMenuIds.contains(recipe.getMenu().getId()),
                    sameRate,
                    totalPrice
            );
        }

        boolean usesOnlyOwnedIngredients() {
            return totalIngredientCount > 0 && ownedIngredientCount == totalIngredientCount;
        }
    }
}