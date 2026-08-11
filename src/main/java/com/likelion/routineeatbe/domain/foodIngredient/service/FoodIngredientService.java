package com.likelion.routineeatbe.domain.foodIngredient.service;

import com.likelion.routineeatbe.domain.foodIngredient.dto.response.FoodIngredientResponse;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.repository.FoodIngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodIngredientService {

    private final FoodIngredientRepository foodIngredientRepository;

    /**
     * - 검색어 입력 받아서 식재료 명 기준으로 필터링
     * 1. StringUtils.hasText(search)으로 검색어가 실제로 있을 때만 필터링 수행
     * 2. 검색어가 있다면 레포에 정의한 검색어 포함하는 객체 리턴 함수 사용
     * 3. 검색어 없으면 바로 findALl() 실행
     * 4. FoodIngredientResponse의 포장함수 from으로 포장하여 리스트 리턴
     * @param search
     * @return 객체 각각을 FoodIngredientResponse로 변환한 리스트 반환
     */
    @Transactional(readOnly = true)
    public List<FoodIngredientResponse> getFoodIngredients(String search){
        List<FoodIngredient> foodIngredients;
        if(StringUtils.hasText(search)){ /* 1. 검색어 유무 검사 */
            /* 2. 검색어 있으니 레포의 findByNameContaining() */
            foodIngredients =foodIngredientRepository.findByNameContaining(search);
        } else{ /* 3. 검색어 없으니 findALl() */
            foodIngredients = foodIngredientRepository.findAll();
        }
        /* 4. (FoodIngredientResponse::from)로 각각을 포장한 리스트 반환 */
        return foodIngredients
                .stream()
                .map(FoodIngredientResponse::from)
                .toList();
    }

    /**
     * - 알레르기 유발 식품 조회
     * allergy 컬럼이 true인 항목만 출력
     * @return
     */
    @Transactional(readOnly = true)
    public List<FoodIngredientResponse> getAllergyFoodIngredients(){
        List<FoodIngredient> foodIngredients=foodIngredientRepository.findByAllergyTrue();

        return foodIngredients.stream()
                .map(FoodIngredientResponse::from)
                .toList();
    }

    /**
     * - 비선호 대표 식품 조회
     * disklike 컬럼이 true인 항목만 출력
     * @return
     */
    @Transactional(readOnly = true)
    public List<FoodIngredientResponse> getDislikeFoodIngredients(){
        List<FoodIngredient> foodIngredients=foodIngredientRepository.findByDislikeTrue();

        return foodIngredients.stream()
                .map(FoodIngredientResponse::from)
                .toList();
    }
}
