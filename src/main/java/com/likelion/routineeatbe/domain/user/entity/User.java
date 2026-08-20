package com.likelion.routineeatbe.domain.user.entity;

import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.favoriteRecipe.entity.FavoriteRecipe;
import com.likelion.routineeatbe.domain.mealPlan.entity.MealPlan;
import com.likelion.routineeatbe.domain.notification.entity.Notification;
import com.likelion.routineeatbe.domain.userSearchHistory.entity.UserSearchHistory;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatistics;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsFoodIngredient;
import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatisticsRecipe;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4, unique = true)
    private String loginNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SkillLevel skillLevel;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<UserFoodIngredient> userFoodIngredients = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<UserCookingEquipment> userCookingEquipments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<UserSearchHistory> userSearchHistories  = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<UserStatistics> userStatistics = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<FavoriteRecipe> favoriteRecipes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<MealPlan> mealPlans = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<CookingRecord> cookingRecords = new ArrayList<>();

    public static User createUser(String loginNumber){
        return User.builder()
                .loginNumber(loginNumber)
                .build();
    }

    public void updateSkillLevel(SkillLevel skillLevel){
        this.skillLevel=skillLevel;
    }


}
