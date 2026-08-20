package com.likelion.routineeatbe.domain.cookingTip.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingTip.enums.CookingTipContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CookingTipTest {

    @Test
    @DisplayName("요리 팁 콘텐츠 생성 성공 - 양방향 연관관계 설정")
    void 요리_팁_콘텐츠_생성_성공() {
        // given
        CookingTip cookingTip = CookingTip.create("대파 써는 법");

        // when
        CookingTipContent content = CookingTipContent.create(
                cookingTip,
                CookingTipContentType.TEXT,
                "대파를 깨끗이 씻어 주세요.",
                1
        );

        // then
        assertThat(content.getCookingTip()).isSameAs(cookingTip);
        assertThat(cookingTip.getContents()).containsExactly(content);
    }

    @Test
    @DisplayName("요리 단계 팁 생성 성공 - 양방향 연관관계 설정")
    void 요리_단계_팁_생성_성공() {
        // given
        CookingStep cookingStep = CookingStep.builder().build();
        CookingTip cookingTip = CookingTip.create("대파 써는 법");

        // when
        CookingStepTip cookingStepTip = CookingStepTip.create(cookingStep, cookingTip);

        // then
        assertThat(cookingStepTip.getCookingStep()).isSameAs(cookingStep);
        assertThat(cookingStepTip.getCookingTip()).isSameAs(cookingTip);
        assertThat(cookingStep.getCookingStepTips()).containsExactly(cookingStepTip);
        assertThat(cookingTip.getCookingStepTips()).containsExactly(cookingStepTip);
    }
}
