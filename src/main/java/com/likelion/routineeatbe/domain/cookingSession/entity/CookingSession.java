package com.likelion.routineeatbe.domain.cookingSession.entity;

import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
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
@Table(name = "cooking_session")
public class CookingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cooking_session_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CookingSessionStatus status;

    @Column(name = "cooking_step_count", nullable = false)
    private Integer cookingStepCount;

    @Builder.Default
    @Column(name = "current_cooking_step_level", nullable = false, columnDefinition = "integer default 1")
    private Integer currentCookingStepLevel = 1;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cooking_record_id", nullable = false, unique = true)
    private CookingRecord cookingRecord;

    @Builder.Default
    @OneToMany(
            mappedBy = "cookingSession",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CookingSessionLog> logs = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "cookingSession",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CookingStep> cookingSteps = new ArrayList<>();

    public static CookingSession create(CookingRecord cookingRecord, Integer cookingStepCount) {
        CookingSession cookingSession = CookingSession.builder()
                .status(CookingSessionStatus.IN_PROGRESS)
                .cookingStepCount(cookingStepCount)
                .currentCookingStepLevel(1)
                .cookingRecord(cookingRecord)
                .build();
        cookingRecord.assignCookingSession(cookingSession);
        return cookingSession;
    }

    public void addCookingStep(CookingStep cookingStep) {
        this.cookingSteps.add(cookingStep);
    }

    public boolean isLastStep() {
        return this.currentCookingStepLevel >= this.cookingStepCount;
    }

    public void moveToNextStep() {
        this.currentCookingStepLevel++;
    }

    public boolean isFirstStep() {
        return this.currentCookingStepLevel <= 1;
    }

    public void moveToPreviousStep() {
        this.currentCookingStepLevel--;
    }

    public void complete() {
        this.status = CookingSessionStatus.COMPLETED;
    }
}
