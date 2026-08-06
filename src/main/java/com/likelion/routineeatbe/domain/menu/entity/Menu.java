package com.likelion.routineeatbe.domain.menu.entity;

import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "menu")
public class Menu extends BaseTimeEntity {

    private static final int DEFAULT_TIME_REQUIRED = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuType type;

    @Column(nullable = false)
    private Double calory;

    @Column(nullable = false, length = 1000)
    private String ingredient_info_original;

    @Column(nullable = false, comment = "분")
    private Integer timeRequired;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficultyLevel;

    private String thumbnailUrl;

    @Builder.Default
    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recipe> recipes = new ArrayList<>();

    public static Menu createFromFoodSafetyKorea(
            String name,
            Double calory,
            String ingredientInfoOriginal
    ) {
        return Menu.builder()
                .name(name)
                .type(MenuType.KOREAN)
                .calory(calory)
                .ingredient_info_original(ingredientInfoOriginal)
                .timeRequired(DEFAULT_TIME_REQUIRED)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build();
    }
}
