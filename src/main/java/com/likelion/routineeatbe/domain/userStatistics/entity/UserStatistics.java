package com.likelion.routineeatbe.domain.userStatistics.entity;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.user.entity.User;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "user_statistics")
public class UserStatistics extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_statistics_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cooked_record_average_difficulty_level", nullable = false)
    private DifficultyLevel cookedRecordAverageDifficultyLevel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(
            mappedBy = "userStatistics",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserStatisticsRecipe> recipes = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "userStatistics",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserStatisticsFoodIngredient> foodIngredients = new ArrayList<>();

    public static UserStatistics create(
            User user,
            DifficultyLevel cookedRecordAverageDifficultyLevel
    ) {
        return UserStatistics.builder()
                .user(user)
                .cookedRecordAverageDifficultyLevel(cookedRecordAverageDifficultyLevel)
                .build();
    }

    public void addRecipe(Recipe recipe) {
        recipes.add(UserStatisticsRecipe.create(this, recipe));
    }

    public void addFoodIngredient(FoodIngredient foodIngredient) {
        foodIngredients.add(UserStatisticsFoodIngredient.create(this, foodIngredient));
    }
}
