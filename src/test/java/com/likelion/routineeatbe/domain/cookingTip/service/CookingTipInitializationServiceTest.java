package com.likelion.routineeatbe.domain.cookingTip.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.likelion.routineeatbe.domain.cookingTip.dto.response.CookingTipInitResDto;
import com.likelion.routineeatbe.domain.cookingTip.exception.CookingTipErrorCode;
import com.likelion.routineeatbe.domain.cookingTip.repository.CookingTipInitializationRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookingTipInitializationServiceTest {

    @InjectMocks
    private CookingTipInitializationService cookingTipInitializationService;

    @Mock
    private CookingTipInitializationRepository cookingTipInitializationRepository;

    @Test
    @DisplayName("요리 팁 초기화 성공 - 전체 팁과 콘텐츠 개수 반환")
    void 요리_팁_초기화_성공() {
        // given
        given(cookingTipInitializationRepository.countCookingTips()).willReturn(68L);
        given(cookingTipInitializationRepository.countCookingTipContents()).willReturn(169L);

        // when
        CookingTipInitResDto result = cookingTipInitializationService.initialize();

        // then
        assertThat(result.cookingTipCount()).isEqualTo(68L);
        assertThat(result.cookingTipContentCount()).isEqualTo(169L);
        then(cookingTipInitializationRepository).should().initialize();
        then(cookingTipInitializationRepository).should().countCookingTips();
        then(cookingTipInitializationRepository).should().countCookingTipContents();
    }

    @Test
    @DisplayName("요리 팁 초기화 실패 - SQL 실행 오류")
    void 요리_팁_초기화_실패_SQL_실행_오류() {
        // given
        willThrow(new IllegalStateException("SQL 실행 오류"))
                .given(cookingTipInitializationRepository)
                .initialize();

        // when & then
        assertThatThrownBy(cookingTipInitializationService::initialize)
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(CookingTipErrorCode.COOKING_TIP_INITIALIZATION_FAILED));
        then(cookingTipInitializationRepository).should().initialize();
        then(cookingTipInitializationRepository).shouldHaveNoMoreInteractions();
    }
}
