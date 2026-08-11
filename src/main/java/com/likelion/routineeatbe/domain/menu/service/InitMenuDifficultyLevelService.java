package com.likelion.routineeatbe.domain.menu.service;

import com.likelion.routineeatbe.domain.menu.dto.MenuDifficultyCalculationDto;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuDifficultyLevelResDto;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.menu.repository.MenuRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitMenuDifficultyLevelService {

    private final MenuRepository menuRepository;
    private final MenuDifficultyLevelCalculator difficultyLevelCalculator;

    /**
     * (1) 작업 목적
     * DB에 저장된 전체 메뉴의 난이도를 조리 시간, 기본 레시피 단계 수, 음식 재료 수로 초기화합니다.
     *
     * (2) 세부 작업 내용
     * - 메뉴별 난이도 계산 데이터를 한 번에 조회합니다.
     * - 세 기준의 점수 합계를 구간별 DifficultyLevel로 변환합니다.
     * - 조회한 모든 메뉴의 난이도를 하나의 트랜잭션으로 갱신합니다.
     *
     * @return 난이도를 초기화한 메뉴 개수를 포함한 응답 DTO
     */
    @Transactional
    public InitMenuDifficultyLevelResDto initialize() {
        log.info(
                "[InitMenuDifficultyLevelService] 메뉴 난이도 초기화 시작 | initialize() - START"
        );
        /*
            1. DTO Projection으로 DifficultyLevel 초기화를 위한 데이터 리스트를 조회한다.
            - Menu
            - 레시피 단계 개수
            - 레시피에 필요함 음식 재료 개수
         */
        List<MenuDifficultyCalculationDto> calculationDtos =
                menuRepository.findAllForDifficultyLevelInitialization();

        /*
            2. 조회한 데이터를 이용해서 각 메뉴별 DifficultyLevel을 계산한다.
            이후 계산한 데이터를 이용해서 Menu의 DifficultyLevel을 갱신한다.
         */
        calculationDtos.forEach(calculationDto -> {
            DifficultyLevel difficultyLevel = difficultyLevelCalculator.calculate(
                    calculationDto.menu().getTimeRequired(),
                    calculationDto.recipeStepCount(),
                    calculationDto.ingredientCount()
            );
            calculationDto.menu().updateDifficultyLevel(difficultyLevel);
        });

        /*
            3. 응답값 반환
            - 초기화 개수
         */
        InitMenuDifficultyLevelResDto result =
                InitMenuDifficultyLevelResDto.create(calculationDtos.size());
        log.info(
                "[InitMenuDifficultyLevelService] 메뉴 난이도 초기화 종료 | initialize() - END | initCount: {}",
                result.initCount()
        );
        return result;
    }
}
