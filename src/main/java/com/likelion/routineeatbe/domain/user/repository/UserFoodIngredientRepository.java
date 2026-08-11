package com.likelion.routineeatbe.domain.user.repository;

import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFoodIngredientRepository extends JpaRepository<UserFoodIngredient,Long> {
    List<UserFoodIngredient> findByUserId(Long id);
    List<UserFoodIngredient> findByUserIdAndRelationType(Long userId, UserFoodIngredientType relationType);

    // 사용자의 특정 관계의 특정 식재료 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserFoodIngredient u " +
            "WHERE u.user.id = :userId " +
            "AND u.relationType = :relationType " +
            "AND u.foodIngredient.id IN :ingredientIds")
    void deleteByUserIdAndTypeAndIngredientIds(
            @Param("userId") Long userId,
            @Param("relationType") UserFoodIngredientType relationType,
            @Param("ingredientIds") List<Long> ingredientIds
    );

    // 사용자의 특정 관계의 특정 식재료들 조회
    List<UserFoodIngredient> findByUserIdAndRelationTypeAndFoodIngredient_IdIn(
            Long userId,
            UserFoodIngredientType relationType,
            List<Long> foodIngredientIds
    );
}
