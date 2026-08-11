package com.likelion.routineeatbe.domain.foodIngredient.repository;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface FoodIngredientRepository extends JpaRepository<FoodIngredient,Long> {

    /**
     * 이름에 검색어가 포함된 음식 재료를 조회합니다.
     *
     * @param name 음식 재료 이름 검색어
     * @return 검색어가 이름에 포함된 음식 재료 목록
     */
    List<FoodIngredient> findByNameContaining(String name);

    /**
     * 전체 음식 재료를 타입과 식별자 순서로 조회합니다.
     *
     * @return 타입별 그룹화에 사용할 음식 재료 목록
     */
    List<FoodIngredient> findAllByOrderByTypeAscIdAsc();

}
