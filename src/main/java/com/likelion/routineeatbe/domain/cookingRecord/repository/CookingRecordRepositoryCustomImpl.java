package com.likelion.routineeatbe.domain.cookingRecord.repository;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingRecordSearchResult;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class CookingRecordRepositoryCustomImpl implements CookingRecordRepositoryCustom {

    private final EntityManager entityManager;

    /**
     * 사용자의 회고 저장까지 종료된 요리 기록을 최신순으로 위치 커서 조회합니다.
     * - 사용자가 평가한 난이도가 저장된 종료 세션만 조회합니다.
     * - size + 1건을 조회하여 다음 데이터 존재 여부를 판별합니다.
     *
     * @param userId 조회할 사용자 PK
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 요리 기록 개수
     * @return 요리 기록 목록 조회 결과 Slice
     */
    @Override
    public Slice<CookingRecordSearchResult> searchTerminatedCookingRecords(
            Long userId,
            Integer cursor,
            Integer size
    ) {
        List<CookingRecordSearchResult> content = new ArrayList<>(entityManager.createQuery("""
                        select new com.likelion.routineeatbe.domain.cookingRecord.dto.CookingRecordSearchResult(
                            recipe.id,
                            menu.name,
                            menu.thumbnailUrl,
                            case when count(distinct favoriteRecipe.id) > 0 then true else false end,
                            cookingRecord.createdAt,
                            cookingRecord.difficultyLevel,
                            count(distinct cookingRecordFoodIngredient.id)
                        )
                        from CookingRecord cookingRecord
                        join cookingRecord.recipe recipe
                        join recipe.menu menu
                        join cookingRecord.cookingSession cookingSession
                        left join cookingRecord.foodIngredients cookingRecordFoodIngredient
                        left join FavoriteRecipe favoriteRecipe
                            on favoriteRecipe.recipe = recipe
                            and favoriteRecipe.user.id = :userId
                        where cookingRecord.user.id = :userId
                          and cookingSession.status = :terminatedStatus
                          and cookingRecord.difficultyLevel is not null
                        group by cookingRecord.id,
                                 cookingRecord.createdAt,
                                 cookingRecord.difficultyLevel,
                                 recipe.id,
                                 menu.name,
                                 menu.thumbnailUrl
                        order by cookingRecord.createdAt desc, cookingRecord.id desc
                        """, CookingRecordSearchResult.class)
                .setParameter("userId", userId)
                .setParameter("terminatedStatus", CookingSessionStatus.TERMINATED)
                .setFirstResult(cursor - 1)
                .setMaxResults(size + 1)
                .getResultList());

        boolean hasNext = content.size() > size;
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
    }
}
