package com.likelion.routineeatbe.domain.recipe.repository;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeReRecommendRequest;
import com.likelion.routineeatbe.domain.recipe.dto.request.RecipeSearchRequestDto;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.enums.RecipeSortType;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class RecipeRepositoryCustomImpl implements RecipeRepositoryCustom {

    private final EntityManager entityManager;

    /**
     * 사용자, 필터, 정렬 및 추천 유형을 기준으로 레시피를 위치 커서 방식으로 조회합니다.
     * - cursor는 레시피 PK가 아니라 1부터 시작하는 정렬 결과의 조회 위치입니다.
     * - size + 1건만 DB에서 조회하여 다음 데이터 존재 여부를 판별합니다.
     * - 반환 페이지의 부족 재료비는 사용자 보유 수량을 차감하여 별도로 집계합니다.
     *
     * @param userId 재료 일치도와 부족 재료비를 계산할 사용자 ID
     * @param request 필터, 정렬, 위치 커서 및 조회 크기
     * @param recommendationType Service에서 지정한 추천 유형
     * @return 레시피 조회 결과 Slice
     */
    @Override
    public Slice<RecipeSearchResult> searchRecipes(
            Long userId,
            RecipeSearchRequestDto request,
            RecommendationType recommendationType
    ) {
        String jpql = createSearchJpql(request, recommendationType);
        TypedQuery<RecipeSearchResult> query = entityManager.createQuery(jpql, RecipeSearchResult.class);
        bindParameters(query, userId, request, recommendationType);

        int offset = Math.toIntExact(request.cursor() - 1L);
        query.setFirstResult(offset);
        query.setMaxResults(request.size() + 1);

        List<RecipeSearchResult> content = new ArrayList<>(query.getResultList());
        boolean hasNext = content.size() > request.size();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        List<RecipeSearchResult> contentWithCost = appendRequiredIngredientCosts(userId, content);
        return new SliceImpl<>(contentWithCost, PageRequest.of(0, request.size()), hasNext);
    }

    /**
     * 사용자가 찜한 레시피를 최신 찜순으로 위치 커서 기반 조회합니다.
     * - size + 1건을 조회하여 다음 데이터 존재 여부를 판별합니다.
     * - 사용자 보유량을 반영하여 부족 재료 개수와 구매 비용을 계산합니다.
     *
     * @param userId 재료 일치도와 부족 재료 정보를 계산할 사용자 ID
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 찜 레시피 개수
     * @return 찜한 레시피 조회 결과 Slice
     */
    @Override
    public Slice<RecipeSearchResult> searchFavoriteRecipes(
            Long userId,
            Long cursor,
            Integer size
    ) {
        List<RecipeSearchResult> content = new ArrayList<>(entityManager.createQuery("""
                        select new com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult(
                            recipe.id,
                            menu.id,
                            menu.name,
                            menu.thumbnailUrl,
                            menu.calory,
                            menu.timeRequired,
                            menu.difficultyLevel,
                            menu.type,
                            recipe.cookingCount,
                            count(distinct userFoodIngredient.foodIngredient.id),
                            count(distinct recipeFoodIngredient.id),
                            cast(0 as long)
                        )
                        from FavoriteRecipe favoriteRecipe
                        join favoriteRecipe.recipe recipe
                        join recipe.menu menu
                        left join RecipeFoodIngredient recipeFoodIngredient
                            on recipeFoodIngredient.recipe = recipe
                        left join UserFoodIngredient userFoodIngredient
                            on userFoodIngredient.foodIngredient = recipeFoodIngredient.foodIngredient
                            and userFoodIngredient.user.id = :userId
                            and userFoodIngredient.relationType = :ownType
                        where favoriteRecipe.user.id = :userId
                        group by favoriteRecipe.id, favoriteRecipe.createdAt, recipe, menu
                        order by favoriteRecipe.createdAt desc, favoriteRecipe.id desc
                        """, RecipeSearchResult.class)
                .setParameter("userId", userId)
                .setParameter("ownType", UserFoodIngredientType.OWN)
                .setFirstResult(Math.toIntExact(cursor - 1L))
                .setMaxResults(size + 1)
                .getResultList());

        boolean hasNext = content.size() > size;
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        List<RecipeSearchResult> result = appendFavoriteIngredientAvailability(userId, content);
        return new SliceImpl<>(result, PageRequest.of(0, size), hasNext);
    }

    /**
     * 메뉴명에 검색어가 포함된 기본 레시피를 일치도 및 인기순으로 조회합니다.
     * - 완전 일치, 접두어 일치, 부분 일치 순으로 정렬합니다.
     * - 같은 일치도에서는 짧은 메뉴명, 요리 횟수, 레시피 PK 순으로 정렬합니다.
     * - size + 1건을 조회하여 다음 데이터 존재 여부를 판별합니다.
     *
     * @param userId 음식 재료 활용률을 계산할 사용자 ID
     * @param searchWord 메뉴/레시피명 검색어
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 레시피 개수
     * @return 사용자 재료 집계가 포함된 검색 레시피 Slice
     */
    @Override
    public Slice<RecipeSearchResult> searchRecipesByMenuName(
            Long userId,
            String searchWord,
            Long cursor,
            Integer size
    ) {
        String normalizedSearchWord = searchWord.toLowerCase(Locale.ROOT);
        String escapedSearchWord = escapeLikePattern(normalizedSearchWord);

        List<RecipeSearchResult> content = new ArrayList<>(entityManager.createQuery("""
                        select new com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult(
                            recipe.id,
                            menu.id,
                            menu.name,
                            menu.thumbnailUrl,
                            menu.calory,
                            menu.timeRequired,
                            menu.difficultyLevel,
                            menu.type,
                            recipe.cookingCount,
                            count(distinct userFoodIngredient.foodIngredient.id),
                            count(distinct recipeFoodIngredient.id),
                            cast(0 as long)
                        )
                        from Recipe recipe
                        join recipe.menu menu
                        left join RecipeFoodIngredient recipeFoodIngredient
                            on recipeFoodIngredient.recipe = recipe
                        left join UserFoodIngredient userFoodIngredient
                            on userFoodIngredient.foodIngredient = recipeFoodIngredient.foodIngredient
                            and userFoodIngredient.user.id = :userId
                            and userFoodIngredient.relationType = :ownType
                        where recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
                          and lower(menu.name) like :containsPattern escape '!'
                        group by recipe, menu
                        order by
                            case
                                when lower(menu.name) = :normalizedSearchWord then 0
                                when lower(menu.name) like :prefixPattern escape '!' then 1
                                else 2
                            end asc,
                            length(menu.name) asc,
                            recipe.cookingCount desc,
                            recipe.id desc
                        """, RecipeSearchResult.class)
                .setParameter("userId", userId)
                .setParameter("ownType", UserFoodIngredientType.OWN)
                .setParameter("normalizedSearchWord", normalizedSearchWord)
                .setParameter("prefixPattern", escapedSearchWord + "%")
                .setParameter("containsPattern", "%" + escapedSearchWord + "%")
                .setFirstResult(Math.toIntExact(cursor - 1L))
                .setMaxResults(size + 1)
                .getResultList());

        boolean hasNext = content.size() > size;
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
    }

    /**
     * 대상 레시피와 음식 재료 구성 차이가 정확히 일치하는 기본 레시피 후보를 조회합니다.
     * - max(대상 재료 수, 후보 재료 수) - 공통 재료 수로 차이를 계산합니다.
     * - 음식 재료의 추가, 제거, 교체를 각각 차이 1로 계산하고 대상 레시피는 제외합니다.
     *
     * @param targetRecipeId 제외할 대상 레시피 PK
     * @param targetFoodIngredientIds 대상 레시피의 음식 재료 PK 집합
     * @param ingredientDifference 조회할 정확한 재료 차이 개수
     * @param limit 최대 조회 개수
     * @return 재료 차이 조건을 만족하는 레시피 후보 목록
     */
    @Override
    public List<Recipe> findRecipeCandidatesByExactIngredientDifference(
            Long targetRecipeId,
            Set<Long> targetFoodIngredientIds,
            int ingredientDifference,
            int limit
    ) {
        String candidateIngredientCountJpql = """
                (select count(candidateIngredient.id)
                 from RecipeFoodIngredient candidateIngredient
                 where candidateIngredient.recipe = recipe)
                """;
        String commonIngredientCountJpql = targetFoodIngredientIds.isEmpty()
                ? "0"
                : """
                        (select count(commonIngredient.id)
                         from RecipeFoodIngredient commonIngredient
                         where commonIngredient.recipe = recipe
                           and commonIngredient.foodIngredient.id in :targetFoodIngredientIds)
                        """;
        String jpql = """
                select recipe
                from Recipe recipe
                join fetch recipe.menu menu
                where recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
                  and recipe.id <> :targetRecipeId
                  and (
                      case
                          when :targetFoodIngredientCount >= %s
                              then :targetFoodIngredientCount
                          else %s
                      end
                      - %s
                  ) = :ingredientDifference
                order by recipe.cookingCount desc, recipe.id desc
                """.formatted(
                candidateIngredientCountJpql,
                candidateIngredientCountJpql,
                commonIngredientCountJpql
        );

        TypedQuery<Recipe> query = entityManager.createQuery(jpql, Recipe.class)
                .setParameter("targetRecipeId", targetRecipeId)
                .setParameter("targetFoodIngredientCount", (long) targetFoodIngredientIds.size())
                .setParameter("ingredientDifference", (long) ingredientDifference)
                .setMaxResults(limit);
        if (!targetFoodIngredientIds.isEmpty()) {
            query.setParameter("targetFoodIngredientIds", targetFoodIngredientIds);
        }
        return query.getResultList();
    }

    /**
     * LIKE 검색에서 특수문자가 와일드카드로 해석되지 않도록 이스케이프합니다.
     * @param searchWord 정규화된 검색어
     * @return LIKE 검색용 이스케이프 문자열
     */
    private String escapeLikePattern(String searchWord) {
        return searchWord
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    /**
     * 레시피 기본 정보, 필요 재료 개수, 사용자 보유 재료 일치 개수를 조회하는 JPQL을 생성합니다.
     * @param request 선택 필터 및 정렬 조건
     * @param recommendationType 조회할 추천 유형
     * @return 실행할 JPQL 문자열
     */
    private String createSearchJpql(
            RecipeSearchRequestDto request,
            RecommendationType recommendationType
    ) {
        StringBuilder jpql = new StringBuilder("""
                select new com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult(
                    recipe.id,
                    menu.id,
                    menu.name,
                    menu.thumbnailUrl,
                    menu.calory,
                    menu.timeRequired,
                    menu.difficultyLevel,
                    menu.type,
                    recipe.cookingCount,
                    count(distinct userFoodIngredient.foodIngredient.id),
                    count(distinct recipeFoodIngredient.id),
                    0L
                )
                from Recipe recipe
                join recipe.menu menu
                left join RecipeFoodIngredient recipeFoodIngredient
                    on recipeFoodIngredient.recipe = recipe
                left join UserFoodIngredient userFoodIngredient
                    on userFoodIngredient.foodIngredient = recipeFoodIngredient.foodIngredient
                    and userFoodIngredient.user.id = :userId
                    and userFoodIngredient.relationType = :ownType
                where recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
                """);

        appendFilterConditions(jpql, request, recommendationType);
        jpql.append(" group by recipe, menu ");
        appendOrderBy(jpql, request.sortType());
        return jpql.toString();
    }

    /**
     * 값이 존재하는 선택 필터와 Service가 지정한 추천 유형 조건을 추가합니다.
     * - DEFAULT는 추천 유형 조건을 생략하여 전체 레시피를 조회합니다.
     * @param jpql 조건을 추가할 JPQL 빌더
     * @param request 시간, 난이도, 카테고리 필터
     * @param recommendationType 조회할 추천 유형
     */
    private void appendFilterConditions(
            StringBuilder jpql,
            RecipeSearchRequestDto request,
            RecommendationType recommendationType
    ) {
        if (recommendationType != RecommendationType.DEFAULT) {
            jpql.append(" and menu.recommendationType = :recommendationType ");
        }
        if (request.timeRequired() != null) {
            jpql.append(" and menu.timeRequired <= :timeRequired ");
        }
        if (request.difficultyLevel() != null) {
            jpql.append(" and menu.difficultyLevel = :difficultyLevel ");
        }
        if (request.category() != null) {
            jpql.append(" and menu.type = :category ");
        }
    }

    /**
     * 정렬 타입에 대응하는 ORDER BY 절을 추가합니다.
     * @param jpql 정렬절을 추가할 JPQL 빌더
     * @param sortType 적용할 정렬 타입
     */
    private void appendOrderBy(StringBuilder jpql, RecipeSortType sortType) {
        if (sortType == RecipeSortType.FOOD_INTEGRATION) {
            jpql.append("""
                    order by
                        case when count(distinct recipeFoodIngredient.id) = 0 then 0.0
                             else count(distinct userFoodIngredient.foodIngredient.id) * 1.0
                                  / count(distinct recipeFoodIngredient.id)
                        end desc,
                        count(distinct userFoodIngredient.foodIngredient.id) desc,
                        recipe.cookingCount desc,
                        recipe.id desc
                    """);
            return;
        }
        jpql.append(" order by recipe.cookingCount desc, recipe.id desc ");
    }

    /**
     * JPQL에 사용자와 선택 필터 값을 바인딩합니다.
     * @param query 파라미터를 바인딩할 TypedQuery
     * @param userId 사용자 ID
     * @param request 실제 필터 값
     * @param recommendationType 조회할 추천 유형
     */
    private void bindParameters(
            TypedQuery<RecipeSearchResult> query,
            Long userId,
            RecipeSearchRequestDto request,
            RecommendationType recommendationType
    ) {
        query.setParameter("userId", userId);
        query.setParameter("ownType", UserFoodIngredientType.OWN);

        if (recommendationType != RecommendationType.DEFAULT) {
            query.setParameter("recommendationType", recommendationType);
        }
        if (request.timeRequired() != null) {
            query.setParameter("timeRequired", request.timeRequired());
        }
        if (request.difficultyLevel() != null) {
            query.setParameter("difficultyLevel", request.difficultyLevel());
        }
        if (request.category() != null) {
            query.setParameter("category", request.category());
        }
    }

    /**
     * 현재 페이지의 레시피별 부족 재료 구매 비용을 계산합니다.
     * - 사용자 OWN 기본 수량을 재료별로 합산합니다.
     * - max(필요 기본 수량 - 보유 기본 수량, 0) / 100 * 100단위 가격을 합산합니다.
     * - 최종 비용은 원 단위 정수로 올림합니다.
     *
     * @param userId 사용자 ID
     * @param content 현재 페이지 조회 결과
     * @return 부족 재료비가 반영된 조회 결과
     */
    private List<RecipeSearchResult> appendRequiredIngredientCosts(
            Long userId,
            List<RecipeSearchResult> content
    ) {
        if (content.isEmpty()) {
            return content;
        }

        Set<Long> recipeIds = content.stream()
                .map(RecipeSearchResult::recipeId)
                .collect(Collectors.toSet());
        List<RecipeFoodIngredient> requiredIngredients = entityManager.createQuery("""
                        select recipeFoodIngredient
                        from RecipeFoodIngredient recipeFoodIngredient
                        join fetch recipeFoodIngredient.foodIngredient
                        where recipeFoodIngredient.recipe.id in :recipeIds
                        """, RecipeFoodIngredient.class)
                .setParameter("recipeIds", recipeIds)
                .getResultList();

        Map<Long, Double> ownedAmountByIngredient = findOwnedAmountByIngredient(
                userId,
                requiredIngredients
        );
        Map<Long, Double> costByRecipe = new HashMap<>();

        for (RecipeFoodIngredient requiredIngredient : requiredIngredients) {
            Long foodIngredientId = requiredIngredient.getFoodIngredient().getId();
            double requiredAmount = requiredIngredient.getPrimaryNeedAmountValue();
            double ownedAmount = ownedAmountByIngredient.getOrDefault(foodIngredientId, 0.0);
            double shortageAmount = Math.max(requiredAmount - ownedAmount, 0.0);
            double ingredientCost = shortageAmount
                    * requiredIngredient.getFoodIngredient().getPricePerHundred()
                    / 100.0;
            costByRecipe.merge(requiredIngredient.getRecipe().getId(), ingredientCost, Double::sum);
        }

        return content.stream()
                .map(result -> result.withRequiredIngredientCost(
                        (long) Math.ceil(costByRecipe.getOrDefault(result.recipeId(), 0.0))
                ))
                .toList();
    }

    /**
     * 찜한 레시피의 부족 재료 개수와 구매 비용을 계산합니다.
     * - 사용자의 OWN 기본 수량을 재료별로 합산합니다.
     * - 필요량보다 보유량이 적은 재료만 부족 재료로 집계합니다.
     *
     * @param userId 사용자 ID
     * @param content 현재 페이지의 찜 레시피 조회 결과
     * @return 부족 재료 개수와 비용이 반영된 조회 결과
     */
    private List<RecipeSearchResult> appendFavoriteIngredientAvailability(
            Long userId,
            List<RecipeSearchResult> content
    ) {
        if (content.isEmpty()) {
            return content;
        }

        Set<Long> recipeIds = content.stream()
                .map(RecipeSearchResult::recipeId)
                .collect(Collectors.toSet());
        List<RecipeFoodIngredient> requiredIngredients = entityManager.createQuery("""
                        select recipeFoodIngredient
                        from RecipeFoodIngredient recipeFoodIngredient
                        join fetch recipeFoodIngredient.foodIngredient
                        where recipeFoodIngredient.recipe.id in :recipeIds
                        """, RecipeFoodIngredient.class)
                .setParameter("recipeIds", recipeIds)
                .getResultList();

        Map<Long, Double> ownedAmountByIngredient = findOwnedAmountByIngredient(
                userId,
                requiredIngredients
        );
        Map<Long, Long> shortageCountByRecipe = new HashMap<>();
        Map<Long, Double> shortageCostByRecipe = new HashMap<>();

        for (RecipeFoodIngredient requiredIngredient : requiredIngredients) {
            Long foodIngredientId = requiredIngredient.getFoodIngredient().getId();
            double requiredAmount = requiredIngredient.getPrimaryNeedAmountValue();
            double ownedAmount = ownedAmountByIngredient.getOrDefault(foodIngredientId, 0.0);
            double shortageAmount = Math.max(requiredAmount - ownedAmount, 0.0);
            if (shortageAmount <= 0.0) {
                continue;
            }

            Long recipeId = requiredIngredient.getRecipe().getId();
            shortageCountByRecipe.merge(recipeId, 1L, Long::sum);
            double ingredientCost = shortageAmount
                    * requiredIngredient.getFoodIngredient().getPricePerHundred()
                    / 100.0;
            shortageCostByRecipe.merge(recipeId, ingredientCost, Double::sum);
        }

        return content.stream()
                .map(result -> result.withRequiredIngredientAvailability(
                        shortageCountByRecipe.getOrDefault(result.recipeId(), 0L),
                        (long) Math.ceil(shortageCostByRecipe.getOrDefault(
                                result.recipeId(),
                                0.0
                        ))
                ))
                .toList();
    }

    /**
     * 사용자의 OWN 기본 수량을 재료별로 합산합니다.
     * @param userId 사용자 ID
     * @param requiredIngredients 현재 페이지의 필요 재료 관계
     * @return 재료 ID별 사용자 보유 기본 수량
     */
    private Map<Long, Double> findOwnedAmountByIngredient(
            Long userId,
            List<RecipeFoodIngredient> requiredIngredients
    ) {
        Set<Long> foodIngredientIds = requiredIngredients.stream()
                .map(recipeFoodIngredient -> recipeFoodIngredient.getFoodIngredient().getId())
                .collect(Collectors.toSet());
        if (foodIngredientIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> ownedAmounts = entityManager.createQuery("""
                        select userFoodIngredient.foodIngredient.id,
                               coalesce(sum(userFoodIngredient.primaryAmountValue), 0.0)
                        from UserFoodIngredient userFoodIngredient
                        where userFoodIngredient.user.id = :userId
                          and userFoodIngredient.relationType = :ownType
                          and userFoodIngredient.foodIngredient.id in :foodIngredientIds
                        group by userFoodIngredient.foodIngredient.id
                        """, Object[].class)
                .setParameter("userId", userId)
                .setParameter("ownType", UserFoodIngredientType.OWN)
                .setParameter("foodIngredientIds", foodIngredientIds)
                .getResultList();

        Map<Long, Double> result = new HashMap<>();
        for (Object[] ownedAmount : ownedAmounts) {
            result.put((Long) ownedAmount[0], ((Number) ownedAmount[1]).doubleValue());
        }
        return result;
    }

    @Override
    public List<Recipe> findCandidateRecipesByDbFilter(
            Set<Long> forbiddenIngredientIds,
            Set<Long> ownedEquipmentIds,
            DifficultyLevel difficultyLevel,
            RecipeReRecommendRequest.CookingTimeFilter timeFilter,
            List<Long> desiredIngredientIds
    ) {
        StringBuilder jpql = new StringBuilder("""
            select distinct recipe
            from Recipe recipe
            join fetch recipe.menu menu
            where recipe.type = com.likelion.routineeatbe.domain.recipe.enums.RecipeType.BASIC
            """);

        Map<String, Object> params = new HashMap<>();

        // 1. 제외 식재료 DB 차단
        if (forbiddenIngredientIds != null && !forbiddenIngredientIds.isEmpty()) {
            jpql.append("""
                and not exists (
                    select 1 from RecipeFoodIngredient rfi
                    where rfi.recipe = recipe
                      and rfi.foodIngredient.id in :forbiddenIngredientIds
                )
                """);
            params.put("forbiddenIngredientIds", forbiddenIngredientIds);
        }

        // 2. 미보유 조리도구 필요 레시피 DB 차단
        if (ownedEquipmentIds == null || ownedEquipmentIds.isEmpty()) {
            jpql.append("""
                and not exists (
                    select 1 from RecipeCookingEquipment rce
                    where rce.recipe = recipe
                )
                """);
        } else {
            jpql.append("""
                and not exists (
                    select 1 from RecipeCookingEquipment rce
                    where rce.recipe = recipe
                      and rce.cookingEquipment.id not in :ownedEquipmentIds
                )
                """);
            params.put("ownedEquipmentIds", ownedEquipmentIds);
        }

        // 3. 동적 필터: 난이도
        if (difficultyLevel != null) {
            jpql.append(" and menu.difficultyLevel = :difficultyLevel ");
            params.put("difficultyLevel", difficultyLevel);
        }

        // 4. 동적 필터: 조리시간 3단계 (QUICK: ~15분, MEDIUM: 15~30분, LONG: 30분~)
        if (timeFilter != null) {
            switch (timeFilter) {
                case QUICK -> jpql.append(" and menu.timeRequired <= 15 ");
                case MEDIUM -> jpql.append(" and menu.timeRequired > 15 and menu.timeRequired <= 30 ");
                case LONG -> jpql.append(" and menu.timeRequired > 30 ");
            }
        }

        // 5. 동적 필터: 희망 식재료 목록 중 최소 1개 포함 조건
        if (desiredIngredientIds != null && !desiredIngredientIds.isEmpty()) {
            jpql.append("""
                and exists (
                    select 1 from RecipeFoodIngredient rfi
                    where rfi.recipe = recipe
                      and rfi.foodIngredient.id in :desiredIngredientIds
                )
                """);
            params.put("desiredIngredientIds", desiredIngredientIds);
        }

        jpql.append(" order by recipe.cookingCount desc, recipe.id desc ");

        TypedQuery<Recipe> query = entityManager.createQuery(jpql.toString(), Recipe.class);
        params.forEach(query::setParameter);

        return query.setMaxResults(40).getResultList();
    }
}
