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
}
