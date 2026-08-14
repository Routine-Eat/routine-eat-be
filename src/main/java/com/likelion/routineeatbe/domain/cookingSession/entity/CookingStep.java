package com.likelion.routineeatbe.domain.cookingSession.entity;

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
@Table(name = "cooking_step")
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
public class CookingStep extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cooking_step_id")
    private Long id;

    @Column(nullable = false)
    private Long level;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "sub_content", length = 500)
    private String subContent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_session_id", nullable = false)
    private CookingSession cookingSession;

    public static CookingStep create(
            CookingSession cookingSession,
            Long level,
            String title,
            String content,
            String subContent
    ) {
        CookingStep cookingStep = CookingStep.builder()
                .level(level)
                .title(title)
                .content(content)
                .subContent(subContent)
                .cookingSession(cookingSession)
                .build();
        cookingSession.addCookingStep(cookingStep);
        return cookingStep;
    }
}
