package com.likelion.routineeatbe.domain.menu.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.likelion.routineeatbe.domain.menu.entity.MenuType;
import com.likelion.routineeatbe.domain.menu.entity.RecommendationType;
import java.util.List;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record MenuAndRecipeMetaDataBatchDto(
        List<MenuMetaData> menus
) {

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MenuMetaData(
            Integer sequence,
            MenuType menuType,
            RecommendationType recommendationType,
            Integer timeRequired
    ) {

        public static MenuMetaData create(
                Integer sequence,
                MenuType menuType,
                RecommendationType recommendationType,
                Integer timeRequired
        ) {
            return MenuMetaData.builder()
                    .sequence(sequence)
                    .menuType(menuType)
                    .recommendationType(recommendationType)
                    .timeRequired(timeRequired)
                    .build();
        }
    }

    public static MenuAndRecipeMetaDataBatchDto create(List<MenuMetaData> menus) {
        return MenuAndRecipeMetaDataBatchDto.builder()
                .menus(menus)
                .build();
    }
}
