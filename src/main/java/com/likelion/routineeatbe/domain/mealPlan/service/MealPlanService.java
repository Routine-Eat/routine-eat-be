package com.likelion.routineeatbe.domain.mealPlan.service;

import com.likelion.routineeatbe.domain.mealPlan.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealPlanService {
    private final MealPlanRepository mealPlanRepository;
}
