package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto.GeneratedCookingStep;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingRecordPersistenceService {

    private static final Set<CookingSessionStatus> BLOCKING_STATUSES =
            EnumSet.of(CookingSessionStatus.IN_PROGRESS, CookingSessionStatus.COMPLETED);
    private static final String CHECK_LIST_TITLE = "요리 시작 전 체크리스트";

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final CookingRecordRepository cookingRecordRepository;

    /**
     * (1) 작업 목적
     * 검증된 Gemini 결과를 요리 기록, 세션, 체크리스트와 요리 단계로 원자적으로 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 행을 잠가 동일 사용자의 동시 요리 시작 요청을 직렬화합니다.
     * - 같은 사용자와 레시피에 진행 중 또는 완료된 세션이 있으면 저장을 차단합니다.
     * - 체크리스트는 level 0, 실제 요리 단계는 level 1 이상으로 저장합니다.
     *
     * @param userId 사용자 PK
     * @param recipeId 레시피 PK
     * @param servings 요청 인분 수
     * @param generated Gemini가 생성한 체크리스트와 요리 단계
     * @return 저장된 요리 기록
     */
    @Transactional
    public CookingRecord save(
            Long userId,
            Long recipeId,
            Integer servings,
            CookingStepGenerateGeminiResponseDto generated
    ) {
        log.info(
                "[CookingRecordPersistenceService] 요리 시작 데이터 저장 시작 | save() - START | userId: {}, recipeId: {}",
                userId,
                recipeId
        );

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.RECIPE_NOT_FOUND));
        if (cookingRecordRepository.existsBlockingSession(userId, recipeId, BLOCKING_STATUSES)) {
            throw new CustomException(CookingRecordErrorCode.COOKING_ALREADY_STARTED);
        }

        CookingRecord cookingRecord = CookingRecord.create(user, recipe, servings);
        CookingSession cookingSession = CookingSession.create(
                cookingRecord,
                generated.cookingSteps().size()
        );
        generated.checkListBeforeStart().forEach(content -> CookingStep.create(
                cookingSession,
                0L,
                CHECK_LIST_TITLE,
                content,
                null
        ));
        for (GeneratedCookingStep cookingStep : generated.cookingSteps()) {
            CookingStep.create(
                    cookingSession,
                    cookingStep.level().longValue(),
                    cookingStep.title(),
                    cookingStep.content(),
                    cookingStep.subContent()
            );
        }

        CookingRecord result = cookingRecordRepository.saveAndFlush(cookingRecord);
        log.info(
                "[CookingRecordPersistenceService] 요리 시작 데이터 저장 종료 | save() - END | cookingRecordId: {}",
                result.getId()
        );
        return result;
    }
}
