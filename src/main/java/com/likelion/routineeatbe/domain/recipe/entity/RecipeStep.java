package com.likelion.routineeatbe.domain.recipe.entity;

import com.likelion.routineeatbe.domain.recipe.enums.RecipeStepType;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "recipe_step")
public class RecipeStep extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipeStepType type;

    @Column(length = 1000)
    private String contents;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public static RecipeStep createNormal(Recipe recipe, long level, String contents) {
        return RecipeStep.builder()
                .level(level)
                .type(RecipeStepType.NORMAL)
                .contents(contents)
                .recipe(recipe)
                .build();
    }
}
