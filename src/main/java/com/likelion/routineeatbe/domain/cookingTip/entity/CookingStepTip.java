package com.likelion.routineeatbe.domain.cookingTip.entity;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "cooking_step_tip",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cooking_step_tip_step_tip",
                columnNames = {"cooking_step_id", "cooking_tip_id"}
        )
)
@AttributeOverrides({
        @AttributeOverride(
                name = "createdAt",
                column = @Column(name = "created_at", nullable = false, updatable = false)
        ),
        @AttributeOverride(
                name = "updatedAt",
                column = @Column(name = "updated_at", nullable = false)
        )
})
public class CookingStepTip extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cooking_step_tip_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_step_id", nullable = false)
    private CookingStep cookingStep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_tip_id", nullable = false)
    private CookingTip cookingTip;

    public static CookingStepTip create(CookingStep cookingStep, CookingTip cookingTip) {
        CookingStepTip cookingStepTip = CookingStepTip.builder()
                .cookingStep(cookingStep)
                .cookingTip(cookingTip)
                .build();
        cookingStep.addCookingStepTip(cookingStepTip);
        cookingTip.addCookingStepTip(cookingStepTip);
        return cookingStepTip;
    }
}
