package com.likelion.routineeatbe.domain.favoriteRecipe.repository;

import com.likelion.routineeatbe.domain.favoriteRecipe.entity.FavoriteRecipe;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRecipeRepository extends JpaRepository<FavoriteRecipe, Long> {

    /**
     * 사용자와 레시피 조합의 찜 존재 여부를 확인합니다.
     *
     * @param userId 사용자 PK
     * @param recipeId 레시피 PK
     * @return 찜이 존재하면 true, 존재하지 않으면 false
     */
    boolean existsByUserIdAndRecipeId(Long userId, Long recipeId);

    /**
     * 사용자와 레시피 조합에 해당하는 찜 정보를 조회합니다.
     *
     * @param userId 사용자 PK
     * @param recipeId 레시피 PK
     * @return 조회된 찜 정보
     */
    Optional<FavoriteRecipe> findByUserIdAndRecipeId(Long userId, Long recipeId);
}
