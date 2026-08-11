package com.likelion.routineeatbe.domain.menu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.menu.dto.MenuDifficultyCalculationDto;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuDifficultyLevelResDto;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InitMenuDifficultyLevelServiceTest {

    @InjectMocks
    private InitMenuDifficultyLevelService initService;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuDifficultyLevelCalculator difficultyLevelCalculator;

    @Test
    @DisplayName("전체 메뉴의 난이도를 계산하여 갱신한다")
    void 전체_메뉴_난이도_초기화_성공() {
        // given
        Menu firstMenu = Menu.builder()
                .id(1L)
                .timeRequired(20)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build();
        Menu secondMenu = Menu.builder()
                .id(2L)
                .timeRequired(70)
                .difficultyLevel(DifficultyLevel.LEVEL_1)
                .build();
        MenuDifficultyCalculationDto firstCalculation =
                MenuDifficultyCalculationDto.create(firstMenu, 7L, 6L);
        MenuDifficultyCalculationDto secondCalculation =
                MenuDifficultyCalculationDto.create(secondMenu, 16L, 15L);
        given(menuRepository.findAllForDifficultyLevelInitialization())
                .willReturn(List.of(firstCalculation, secondCalculation));
        given(difficultyLevelCalculator.calculate(20, 7L, 6L))
                .willReturn(DifficultyLevel.LEVEL_2);
        given(difficultyLevelCalculator.calculate(70, 16L, 15L))
                .willReturn(DifficultyLevel.LEVEL_5);

        // when
        InitMenuDifficultyLevelResDto result = initService.initialize();

        // then
        assertThat(result.initCount()).isEqualTo(2L);
        assertThat(firstMenu.getDifficultyLevel()).isEqualTo(DifficultyLevel.LEVEL_2);
        assertThat(secondMenu.getDifficultyLevel()).isEqualTo(DifficultyLevel.LEVEL_5);
    }

    @Test
    @DisplayName("메뉴가 없으면 난이도 계산 없이 0을 반환한다")
    void 초기화_대상_메뉴_없음_0_반환_성공() {
        // given
        given(menuRepository.findAllForDifficultyLevelInitialization()).willReturn(List.of());

        // when
        InitMenuDifficultyLevelResDto result = initService.initialize();

        // then
        assertThat(result.initCount()).isZero();
        then(difficultyLevelCalculator).shouldHaveNoInteractions();
    }
}
