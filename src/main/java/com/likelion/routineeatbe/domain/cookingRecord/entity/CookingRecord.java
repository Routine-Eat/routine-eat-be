package com.likelion.routineeatbe.domain.cookingRecord.entity;

import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "cooking_record")
public class CookingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cooking_record_id")
    private Long id;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "integer default 1")
    private Integer servings = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "taste_rating")
    private TasteRating tasteRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_rating")
    private DifficultyLevel difficultyRating;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Builder.Default
    @OneToMany(
            mappedBy = "cookingRecord",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CookingRecordFoodIngredient> foodIngredients = new ArrayList<>();

    @OneToOne(
            mappedBy = "cookingRecord",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private CookingSession cookingSession;

    public static CookingRecord create(User user, Recipe recipe, Integer servings) {
        return CookingRecord.builder()
                .user(user)
                .recipe(recipe)
                .servings(servings)
                .build();
    }

    public void assignCookingSession(CookingSession cookingSession) {
        this.cookingSession = cookingSession;
    }

    public void addFoodIngredient(CookingRecordFoodIngredient foodIngredient) {
        this.foodIngredients.add(foodIngredient);
    }

    public void saveCookingResult(
            TasteRating tasteRating,
            DifficultyLevel difficultyLevel,
            String photoUrl
    ) {
        this.tasteRating = tasteRating;
        this.difficultyRating = difficultyLevel;
        if (photoUrl != null) {
            this.photoUrl = photoUrl;
        }
    }
}
