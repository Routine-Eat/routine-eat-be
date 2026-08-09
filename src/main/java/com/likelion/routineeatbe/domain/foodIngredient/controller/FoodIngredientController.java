package com.likelion.routineeatbe.domain.foodIngredient.controller;

import com.likelion.routineeatbe.domain.foodIngredient.dto.response.FoodIngredientResponse;
import com.likelion.routineeatbe.domain.foodIngredient.repository.FoodIngredientRepository;
import com.likelion.routineeatbe.domain.foodIngredient.service.FoodIngredientService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FoodIngredientController implements FoodIngredientControllerDocs{

    private final FoodIngredientService foodIngredientService;

    @Override
    public GlobalResponse<List<FoodIngredientResponse>> getFoodIngredient(String search){
        List<FoodIngredientResponse> foodIngredientResponseList=foodIngredientService.getFoodIngredient(search);
        return GlobalResponse.success("식재료 조회가 성공했습니다.",foodIngredientResponseList);
    }
}
