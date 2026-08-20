package com.likelion.routineeatbe.domain.cookingTip.entity;

import com.likelion.routineeatbe.domain.cookingTip.enums.CookingTipContentType;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
        name = "cooking_tip_content",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cooking_tip_content_tip_sort_order",
                columnNames = {"cooking_tip_id", "sort_order"}
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
public class CookingTipContent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cooking_tip_content_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CookingTipContentType type;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_tip_id", nullable = false)
    private CookingTip cookingTip;

    public static CookingTipContent create(
            CookingTip cookingTip,
            CookingTipContentType type,
            String content,
            Integer sortOrder
    ) {
        CookingTipContent cookingTipContent = CookingTipContent.builder()
                .cookingTip(cookingTip)
                .type(type)
                .content(content)
                .sortOrder(sortOrder)
                .build();
        cookingTip.addContent(cookingTipContent);
        return cookingTipContent;
    }
}
