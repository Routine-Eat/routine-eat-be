package com.likelion.routineeatbe.domain.user.repository;

import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFoodIngredientRepository extends JpaRepository<UserFoodIngredient,Long> {
    List<UserFoodIngredient> findByUserId(Long id);
    List<UserFoodIngredient> findByUserIdAndRelationType(Long userId, UserFoodIngredientType relationType);

    /**
     * 사용자의 특정 관계 음식 재료를 음식 재료 Entity와 함께 조회합니다.
     * @param userId 조회할 사용자 PK
     * @param relationType 조회할 사용자 음식 재료 관계
     * @return 음식 재료가 함께 조회된 사용자 음식 재료 목록
     */
    @Query("""
            select userFoodIngredient
            from UserFoodIngredient userFoodIngredient
            join fetch userFoodIngredient.foodIngredient foodIngredient
            where userFoodIngredient.user.id = :userId
              and userFoodIngredient.relationType = :relationType
            order by foodIngredient.id, userFoodIngredient.id
            """)
    List<UserFoodIngredient> findAllWithFoodIngredientByUserIdAndRelationType(
            @Param("userId") Long userId,
            @Param("relationType") UserFoodIngredientType relationType
    );

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

    /**
     * 사용자의 특정 관계 음식 재료를 비관적 쓰기 잠금으로 조회합니다.
     * 동일 음식 재료가 여러 행이면 생성 순서대로 차감할 수 있도록 PK 순으로 정렬합니다.
     *
     * @param userId 조회할 사용자 PK
     * @param relationType 조회할 사용자 음식 재료 관계
     * @param foodIngredientIds 조회할 음식 재료 PK 목록
     * @return 잠금이 적용된 사용자 음식 재료 목록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select userFoodIngredient
            from UserFoodIngredient userFoodIngredient
            join fetch userFoodIngredient.foodIngredient foodIngredient
            where userFoodIngredient.user.id = :userId
              and userFoodIngredient.relationType = :relationType
              and foodIngredient.id in :foodIngredientIds
            order by foodIngredient.id, userFoodIngredient.id
            """)
    List<UserFoodIngredient> findAllForUpdateByUserIdAndRelationTypeAndFoodIngredientIds(
            @Param("userId") Long userId,
            @Param("relationType") UserFoodIngredientType relationType,
            @Param("foodIngredientIds") List<Long> foodIngredientIds
    );
}
