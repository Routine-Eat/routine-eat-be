package com.likelion.routineeatbe.domain.mealPlan.controller;

import com.likelion.routineeatbe.domain.mealPlan.service.MealPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MealPlanController implements MealPlanControllerDocs{
    private final MealPlanService mealPlanService;
}
