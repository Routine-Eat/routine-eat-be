package com.likelion.routineeatbe.domain.cookingTip.entity;

import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "cooking_tip",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cooking_tip_title",
                columnNames = "title"
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
public class CookingTip extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cooking_tip_id")
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Builder.Default
    @OrderBy("sortOrder ASC")
    @OneToMany(
            mappedBy = "cookingTip",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CookingTipContent> contents = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "cookingTip",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CookingStepTip> cookingStepTips = new ArrayList<>();

    public static CookingTip create(String title) {
        return CookingTip.builder()
                .title(title)
                .build();
    }

    public void addContent(CookingTipContent content) {
        this.contents.add(content);
    }

    public void addCookingStepTip(CookingStepTip cookingStepTip) {
        this.cookingStepTips.add(cookingStepTip);
    }
}
