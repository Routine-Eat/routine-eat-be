package com.likelion.routineeatbe.domain.cookingTip.service;

import com.likelion.routineeatbe.domain.cookingTip.dto.response.CookingTipInitResDto;
import com.likelion.routineeatbe.domain.cookingTip.exception.CookingTipErrorCode;
import com.likelion.routineeatbe.domain.cookingTip.repository.CookingTipInitializationRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingTipInitializationService {

    private final CookingTipInitializationRepository cookingTipInitializationRepository;

    /**
     * (1) 작업 목적
     * SQL 시드 파일을 실행하여 요리 팁과 요리 팁 콘텐츠를 초기화합니다.
     *
     * (2) 세부 작업 내용
     * - 요리 팁 SQL 시드 파일을 실행합니다.
     * - 실행 완료 후 전체 요리 팁과 콘텐츠 개수를 조회합니다.
     * - SQL 실행 또는 개수 조회에 실패하면 요리 팁 초기화 예외를 발생시킵니다.
     *
     * @return 초기화 후 전체 요리 팁과 콘텐츠 개수를 포함한 응답 DTO
     */
    public CookingTipInitResDto initialize() {
        log.info(
                "[CookingTipInitializationService] 요리 팁 초기화 시작 | initialize() - START"
        );

        try {
            /*
                1. 요리 팁 SQL 시드 파일 실행
                - CookingTip과 CookingTipContent 데이터를 SQL 파일의 내용으로 초기화한다.
             */
            cookingTipInitializationRepository.initialize();

            /*
                2. 초기화 결과 개수 조회
                - 초기화 완료 후 DB에 저장된 전체 요리 팁과 콘텐츠 개수를 조회한다.
             */
            long cookingTipCount = cookingTipInitializationRepository.countCookingTips();
            long cookingTipContentCount =
                    cookingTipInitializationRepository.countCookingTipContents();

            /*
                3. 응답 DTO 생성
                - 조회한 전체 데이터 개수를 초기화 응답으로 변환한다.
             */
            CookingTipInitResDto result = CookingTipInitResDto.create(
                    cookingTipCount,
                    cookingTipContentCount
            );

            log.info(
                    "[CookingTipInitializationService] 요리 팁 초기화 종료 | initialize() - END | cookingTipCount: {}, cookingTipContentCount: {}",
                    result.cookingTipCount(),
                    result.cookingTipContentCount()
            );
            return result;
        } catch (RuntimeException exception) {
            log.error(
                    "[CookingTipInitializationService] 요리 팁 초기화 실패 | initialize() - ERROR",
                    exception
            );
            throw new CustomException(CookingTipErrorCode.COOKING_TIP_INITIALIZATION_FAILED);
        }
    }
}
