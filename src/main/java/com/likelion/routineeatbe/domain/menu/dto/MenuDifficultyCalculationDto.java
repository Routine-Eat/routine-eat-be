package com.likelion.routineeatbe.domain.menu.dto;

import com.likelion.routineeatbe.domain.menu.entity.Menu;

public record MenuDifficultyCalculationDto(
        Menu menu,
        long recipeStepCount,
        long ingredientCount
) {

    public static MenuDifficultyCalculationDto create(
            Menu menu,
            long recipeStepCount,
            long ingredientCount
    ) {
        return new MenuDifficultyCalculationDto(menu, recipeStepCount, ingredientCount);
    }
}
