package com.likelion.routineeatbe.domain.menu.dto;

import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import lombok.Builder;

@Builder
public record MenuAndRecipeMetaDataDto(
        MenuType menuType,
        RecommendationType recommendationType,
        Integer timeRequired,
        String thumbnailUrl
) {

    public static MenuAndRecipeMetaDataDto create(
            MenuType menuType,
            RecommendationType recommendationType,
            Integer timeRequired,
            String thumbnailUrl
    ) {
        return MenuAndRecipeMetaDataDto.builder()
                .menuType(menuType)
                .recommendationType(recommendationType)
                .timeRequired(timeRequired)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }
}
