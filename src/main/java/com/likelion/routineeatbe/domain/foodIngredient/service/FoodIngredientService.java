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
     * - StringUtils.hasText(search)으로 검색어가 실제로 있을 때만 필터링 수행
     * - 검색어 없으면 바로 findALl() 실행
     * @param search
     * @return 객체 각각을 FoodIngredientResponse로 변환한 리스트 반환
     */
    @Transactional
    public List<FoodIngredientResponse> getFoodIngredient(String search){
        List<FoodIngredient> foodIngredients;
        if(StringUtils.hasText(search)){
            foodIngredients =foodIngredientRepository.findByNameContaining(search);
        } else{
            foodIngredients = foodIngredientRepository.findAll();
        }
        return foodIngredients
                .stream()
                .map(FoodIngredientResponse::from)
                .toList();
    }
}
