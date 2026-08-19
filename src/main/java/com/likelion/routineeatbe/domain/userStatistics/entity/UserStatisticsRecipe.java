package com.likelion.routineeatbe.domain.userStatistics.entity;

import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_statistics_recipe")
public class UserStatisticsRecipe extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_statistics_recipe_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_statistics_id", nullable = false)
    private UserStatistics userStatistics;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public static UserStatisticsRecipe create(
            UserStatistics userStatistics,
            Recipe recipe
    ) {
        return UserStatisticsRecipe.builder()
                .userStatistics(userStatistics)
                .recipe(recipe)
                .build();
    }
}
