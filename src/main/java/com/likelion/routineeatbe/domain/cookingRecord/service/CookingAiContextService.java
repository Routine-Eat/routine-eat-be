package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiContextDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.mapper.CookingRecordMapper;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSessionLog;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionLogType;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingSessionLogRepository;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingSessionRepository;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingStepRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingAiContextService {

    private static final long FIRST_COOKING_STEP_LEVEL = 1L;

    private final UserRepository userRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final CookingSessionRepository cookingSessionRepository;
    private final CookingStepRepository cookingStepRepository;
    private final RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    private final CookingSessionLogRepository cookingSessionLogRepository;
    private final CookingRecordMapper cookingRecordMapper;

    /**
     * (1) 작업 목적
     * AI 요청에 필요한 요리 컨텍스트를 조회하고 사용자 발화를 로그에 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자와 사용자 소유 요리 기록을 조회합니다.
     * - 진행 중인 세션과 실제 요리 단계, 레시피 음식 재료를 조회합니다.
     * - 사용자 발화를 USER 로그로 저장하고 외부 호출에 사용할 DTO로 변환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @param userSpeechText 사용자의 발화 텍스트
     * @return Gemini 요청에 사용할 요리 컨텍스트
     */
    @Transactional
    public CookingAiContextDto prepareContext(
            Long cookingRecordId,
            String userNumber,
            String userSpeechText
    ) {
        log.info(
                "[CookingAiContextService] 요리 AI 컨텍스트 준비 시작 | prepareContext() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdWithRecipeAndMenu(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));
        CookingSession cookingSession = validateInProgressSession(cookingRecord);
        List<CookingStep> cookingSteps = cookingStepRepository
                .findAllByCookingSessionIdAndLevelGreaterThanEqualOrderByLevelAsc(
                        cookingSession.getId(),
                        FIRST_COOKING_STEP_LEVEL
                );
        if (cookingSteps.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.COOKING_STEP_NOT_FOUND);
        }
        List<RecipeFoodIngredient> foodIngredients = recipeFoodIngredientRepository
                .findAllByRecipeIdInWithFoodIngredient(List.of(cookingRecord.getRecipe().getId()));
        if (foodIngredients.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.RECIPE_FOOD_INGREDIENT_EMPTY);
        }
        cookingSessionLogRepository.save(CookingSessionLog.create(
                cookingSession,
                CookingSessionLogType.USER,
                userSpeechText
        ));
        CookingAiContextDto result = cookingRecordMapper.toCookingAiContextDto(
                cookingRecord,
                cookingSteps,
                foodIngredients
        );

        log.info(
                "[CookingAiContextService] 요리 AI 컨텍스트 준비 종료 | prepareContext() - END | cookingSessionId: {}, cookingStepCount: {}",
                result.cookingSessionId(),
                result.cookingSteps().size()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * Gemini가 생성한 텍스트 응답을 AI 로그로 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 요리 세션을 조회합니다.
     * - 생성된 답변을 CookingSessionLogType.AI로 저장합니다.
     *
     * @param cookingSessionId 요리 세션 PK
     * @param answer Gemini가 생성한 답변
     */
    @Transactional
    public void saveAiLog(Long cookingSessionId, String answer) {
        log.info(
                "[CookingAiContextService] AI 응답 로그 저장 시작 | saveAiLog() - START | cookingSessionId: {}",
                cookingSessionId
        );

        CookingSession cookingSession = cookingSessionRepository.findById(cookingSessionId)
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND
                ));
        cookingSessionLogRepository.save(CookingSessionLog.create(
                cookingSession,
                CookingSessionLogType.AI,
                answer
        ));

        log.info(
                "[CookingAiContextService] AI 응답 로그 저장 종료 | saveAiLog() - END | cookingSessionId: {}",
                cookingSessionId
        );
    }

    /**
     * (1) 작업 목적
     * AI 명령으로 실행된 시스템 동작 결과를 로그로 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 요리 세션을 조회합니다.
     * - 시스템 동작 설명을 CookingSessionLogType.SYSTEM으로 저장합니다.
     *
     * @param cookingSessionId 요리 세션 PK
     * @param content 시스템 동작 결과 설명
     */
    @Transactional
    public void saveSystemLog(Long cookingSessionId, String content) {
        log.info(
                "[CookingAiContextService] 시스템 동작 로그 저장 시작 | saveSystemLog() - START | cookingSessionId: {}",
                cookingSessionId
        );

        CookingSession cookingSession = cookingSessionRepository.findById(cookingSessionId)
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND
                ));
        cookingSessionLogRepository.save(CookingSessionLog.create(
                cookingSession,
                CookingSessionLogType.SYSTEM,
                content
        ));

        log.info(
                "[CookingAiContextService] 시스템 동작 로그 저장 종료 | saveSystemLog() - END | cookingSessionId: {}",
                cookingSessionId
        );
    }

    /**
     * 요리 기록에 진행 중인 세션이 연결되어 있는지 검증합니다.
     *
     * @param cookingRecord 검증할 요리 기록
     * @return 검증된 진행 중 요리 세션
     */
    private CookingSession validateInProgressSession(CookingRecord cookingRecord) {
        log.debug(
                "[CookingAiContextService] 진행 중 요리 세션 검증 시작 | validateInProgressSession() - START | cookingRecordId: {}",
                cookingRecord.getId()
        );
        CookingSession cookingSession = cookingRecord.getCookingSession();
        if (cookingSession == null) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND);
        }
        if (cookingSession.getStatus() != CookingSessionStatus.IN_PROGRESS) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_IN_PROGRESS);
        }
        log.debug(
                "[CookingAiContextService] 진행 중 요리 세션 검증 종료 | validateInProgressSession() - END | cookingSessionId: {}",
                cookingSession.getId()
        );
        return cookingSession;
    }
}
