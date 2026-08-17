package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingRecordSearchResult;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingRecordSearchReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingResultSaveReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.mapper.CookingRecordMapper;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingStepGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingStepRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeStepRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingRecordService {

    private static final Set<CookingSessionStatus> BLOCKING_STATUSES =
            EnumSet.of(CookingSessionStatus.IN_PROGRESS, CookingSessionStatus.COMPLETED);

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final CookingStepRepository cookingStepRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final CookingStepGenerateGeminiService geminiService;
    private final CookingRecordPersistenceService persistenceService;
    private final CookingRecordImageStorageService imageStorageService;
    private final CookingRecordMapper cookingRecordMapper;

    /**
     * (1) 작업 목적
     * 사용자의 회고 저장까지 종료된 요리 기록을 최신순으로 조회합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호로 사용자를 조회합니다.
     * - 종료 상태와 사용자 난이도가 저장된 요리 기록을 위치 커서 기반 조회합니다.
     * - 다음 조회 위치를 계산하고 요리 기록 목록 응답으로 변환합니다.
     *
     * @param request 사용자 식별번호와 커서 조회 조건
     * @return 요리 기록 목록과 다음 커서 정보
     */
    @Transactional(readOnly = true)
    public CookingRecordListResDto getCookingRecords(CookingRecordSearchReqDto request) {
        log.info(
                "[CookingRecordService] 요리 기록 목록 조회 시작 | getCookingRecords() - START | userNumber: {}, cursor: {}, size: {}",
                request.userNumber(),
                request.cursor(),
                request.size()
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));

        /*
            2. 종료 요리 기록 조회
            - 회고 저장까지 종료된 요리 기록을 최신순으로 위치 커서 조회합니다.
         */
        Slice<CookingRecordSearchResult> cookingRecordSlice = cookingRecordRepository
                .searchTerminatedCookingRecords(
                        user.getId(),
                        request.cursor(),
                        request.size()
                );

        /*
            3. 요리 기록 목록 응답 변환
            - 다음 데이터가 존재하면 다음 조회 위치를 계산하고 Mapper로 응답을 생성합니다.
         */
        Integer nextCursor = cookingRecordSlice.hasNext()
                ? request.cursor() + request.size()
                : null;
        CookingRecordListResDto result = cookingRecordMapper.toCookingRecordListResDto(
                cookingRecordSlice,
                nextCursor
        );

        log.info(
                "[CookingRecordService] 요리 기록 목록 조회 종료 | getCookingRecords() - END | resultSize: {}, nextCursor: {}",
                result.content().size(),
                result.nextCursor()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자 소유 요리 기록의 메뉴 정보와 저장된 회고를 상세 조회합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호로 사용자를 조회합니다.
     * - 사용자 소유 요리 기록을 레시피와 메뉴까지 함께 조회합니다.
     * - 메뉴 난이도와 사용자가 평가한 난이도를 구분하여 상세 응답으로 변환합니다.
     *
     * @param cookingRecordId 조회할 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 메뉴 정보와 사용자 회고가 포함된 요리 기록 상세 응답
     */
    @Transactional(readOnly = true)
    public CookingRecordDetailResDto getCookingRecordDetail(
            Long cookingRecordId,
            String userNumber
    ) {
        log.info(
                "[CookingRecordService] 요리 기록 상세 조회 시작 | getCookingRecordDetail() - START | cookingRecordId: {}, userNumber: {}",
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
        CookingRecordDetailResDto result = cookingRecordMapper
                .toCookingRecordDetailResDto(cookingRecord);

        log.info(
                "[CookingRecordService] 요리 기록 상세 조회 종료 | getCookingRecordDetail() - END | cookingRecordId: {}",
                result.cookingRecordId()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 완료된 요리 기록의 재료 사용 전후 사용자 보유 예상량을 조회합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호로 사용자를 조회합니다.
     * - 사용자 소유 요리 기록을 세션, 레시피와 사용 음식 재료까지 함께 조회합니다.
     * - 요리 세션이 완료 상태인지 검증합니다.
     * - 사용자의 현재 보유량과 요리 시작 시 초기화된 사용량을 차감 전후 예상량으로 변환합니다.
     *
     * @param cookingRecordId 조회할 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 요리 전후 사용자 보유 예상량 목록
     */
    @Transactional(readOnly = true)
    public CookingRecordFoodIngredientsResDto getFoodIngredients(
            Long cookingRecordId,
            String userNumber
    ) {
        log.info(
                "[CookingRecordService] 요리 사용 음식 재료 조회 시작 | getFoodIngredients() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdWithFoodIngredients(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));
        CookingSession cookingSession = cookingRecord.getCookingSession();
        if (cookingSession == null) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND);
        }
        if (cookingSession.getStatus() != CookingSessionStatus.COMPLETED) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_COMPLETED);
        }
        List<RecipeFoodIngredient> recipeFoodIngredients = recipeFoodIngredientRepository
                .findAllByRecipeIdInWithFoodIngredient(List.of(cookingRecord.getRecipe().getId()));
        if (recipeFoodIngredients.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.RECIPE_FOOD_INGREDIENT_EMPTY);
        }
        Set<Long> cookingRecordFoodIngredientIds = cookingRecord.getFoodIngredients().stream()
                .map(foodIngredient -> foodIngredient.getFoodIngredient().getId())
                .collect(Collectors.toSet());
        boolean allFoodIngredientsInitialized = recipeFoodIngredients.stream()
                .map(recipeFoodIngredient -> recipeFoodIngredient.getFoodIngredient().getId())
                .allMatch(cookingRecordFoodIngredientIds::contains);
        if (!allFoodIngredientsInitialized) {
            throw new CustomException(
                    CookingRecordErrorCode.COOKING_RECORD_FOOD_INGREDIENT_EMPTY
            );
        }
        List<UserFoodIngredient> ownedFoodIngredients = userFoodIngredientRepository
                .findAllWithFoodIngredientByUserIdAndRelationTypeAndFoodIngredientIds(
                        user.getId(),
                        UserFoodIngredientType.OWN,
                        cookingRecordFoodIngredientIds.stream().sorted().toList()
                );

        CookingRecordFoodIngredientsResDto result = cookingRecordMapper
                .toCookingRecordFoodIngredientsResDto(
                        cookingRecord,
                        recipeFoodIngredients,
                        ownedFoodIngredients
                );
        log.info(
                "[CookingRecordService] 요리 사용 음식 재료 조회 종료 | getFoodIngredients() - END | cookingRecordId: {}, foodIngredientCount: {}",
                cookingRecordId,
                result.foodIngredients().size()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자의 최근 완료 요리 기록에 맛 평가, 난이도와 선택 이미지를 저장합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자의 가장 최근 완료 요리 기록을 조회합니다.
     * - 선택 이미지가 있으면 S3에 업로드한 후 별도 트랜잭션에서 회고를 저장합니다.
     * - 요청된 음식 재료 사용량을 보정한 뒤 수정된 사용량으로 사용자 보유량을 차감합니다.
     * - DB 저장 실패 시 먼저 업로드된 S3 객체를 보상 삭제합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @param request 맛 평가와 실제 요리 난이도
     * @param image 선택 요리 결과 이미지
     * @return 저장된 요리 기록 PK
     */
    public CookingResultSaveResDto saveCookingResult(
            String userNumber,
            CookingResultSaveReqDto request,
            MultipartFile image
    ) {
        log.info(
                "[CookingRecordService] 요리 결과 저장 시작 | saveCookingResult() - START | userNumber: {}",
                userNumber
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                        user.getId(),
                        CookingSessionStatus.COMPLETED
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COMPLETED_COOKING_RECORD_NOT_FOUND
                ));

        boolean imageUploaded = image != null && !image.isEmpty();
        String photoUrl = imageUploaded
                ? imageStorageService.upload(user.getId(), cookingRecord.getId(), image)
                : null;
        CookingRecord savedCookingRecord;
        try {
            savedCookingRecord = persistenceService.saveCookingResult(
                    user.getId(),
                    cookingRecord.getId(),
                    request.tasteRating(),
                    request.difficultyLevel(),
                    request.cookingTip(),
                    request.modifiedCookingRecordFoodIngredients(),
                    photoUrl
            );
        } catch (RuntimeException exception) {
            if (imageUploaded) {
                imageStorageService.delete(
                        user.getId(),
                        cookingRecord.getId(),
                        image.getOriginalFilename()
                );
            }
            throw exception;
        }

        CookingResultSaveResDto result =
                cookingRecordMapper.toCookingResultSaveResDto(savedCookingRecord);
        log.info(
                "[CookingRecordService] 요리 결과 저장 종료 | saveCookingResult() - END | cookingRecordId: {}",
                result.savedCookingRecordId()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자와 레시피 정보를 바탕으로 맞춤 요리 단계를 생성하고 요리를 시작합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자, 레시피, 재료와 기존 단계를 조회하고 중복 요리 시작 여부를 확인합니다.
     * - Gemini로 체크리스트와 요리 단계를 생성한 후 하나의 트랜잭션으로 저장합니다.
     * - 저장된 1번 단계의 상세 정보와 생성 데이터를 요리 시작 응답 DTO로 변환합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @param request 레시피 PK와 요청 인분 수
     * @return 요리 시작 결과
     */
    public CookingStartResDto startCooking(
            String userNumber,
            CookingStartReqDto request
    ) {
        log.info(
                "[CookingRecordService] 요리 시작 시작 | startCooking() - START | userNumber: {}, recipeId: {}, servings: {}",
                userNumber,
                request.recipeId(),
                request.servings()
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        Recipe recipe = recipeRepository.findByIdWithMenu(request.recipeId())
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.RECIPE_NOT_FOUND));
        if (cookingRecordRepository.existsBlockingSession(
                user.getId(),
                recipe.getId(),
                BLOCKING_STATUSES
        )) {
            throw new CustomException(CookingRecordErrorCode.COOKING_ALREADY_STARTED);
        }

        List<RecipeFoodIngredient> ingredients = recipeFoodIngredientRepository
                .findAllByRecipeIdInWithFoodIngredient(List.of(recipe.getId()));
        if (ingredients.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.RECIPE_FOOD_INGREDIENT_EMPTY);
        }
        List<RecipeStep> recipeSteps = recipeStepRepository
                .findAllByRecipeIdOrderByLevelAsc(recipe.getId());
        if (recipeSteps.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.RECIPE_STEP_EMPTY);
        }

        CookingStepGenerateGeminiResponseDto generated = geminiService.generate(
                user,
                recipe,
                ingredients,
                recipeSteps,
                request.servings()
        );
        CookingRecord cookingRecord = persistenceService.save(
                user.getId(),
                recipe.getId(),
                request.servings(),
                generated
        );
        CookingSession cookingSession = cookingRecord.getCookingSession();
        CookingStep firstCookingStep = cookingStepRepository
                .findByCookingSessionIdAndLevel(cookingSession.getId(), 1L)
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        CookingStartResDto result = cookingRecordMapper.toCookingStartResDto(
                cookingRecord,
                recipe,
                generated,
                firstCookingStep
        );

        log.info(
                "[CookingRecordService] 요리 시작 종료 | startCooking() - END | cookingRecordId: {}",
                result.cookingRecordId()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자의 요리 세션을 다음 요리 단계로 이동하거나 마지막 단계에서 완료 처리합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 소유 요리 기록과 세션을 비관적 쓰기 잠금으로 조회합니다.
     * - 진행 중 세션의 현재 단계가 마지막이면 완료 상태로 변경합니다.
     * - 마지막 단계가 아니면 현재 단계를 증가시키고 다음 단계 상세 정보를 반환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 이동한 요리 단계 정보, 요리 완료 시 null
     */
    @Transactional
    public CookingStepNavigationResDto moveToNextCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        log.info(
                "[CookingRecordService] 다음 요리 단계 이동 시작 | moveToNextCookingStep() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdForUpdate(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));
        CookingSession cookingSession = cookingRecord.getCookingSession();
        if (cookingSession == null) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND);
        }
        if (cookingSession.getStatus() != CookingSessionStatus.IN_PROGRESS) {
            throw new CustomException(
                    CookingRecordErrorCode.COOKING_SESSION_NOT_IN_PROGRESS
            );
        }
        validateCookingStepState(cookingSession);

        if (cookingSession.isLastStep()) {
            cookingSession.complete();
            log.info(
                    "[CookingRecordService] 다음 요리 단계 이동 종료 | moveToNextCookingStep() - END | cookingRecordId: {}, status: {}",
                    cookingRecordId,
                    cookingSession.getStatus()
            );
            return null;
        }

        cookingSession.moveToNextStep();
        CookingStep cookingStep = cookingStepRepository.findByCookingSessionIdAndLevel(
                        cookingSession.getId(),
                        cookingSession.getCurrentCookingStepLevel().longValue()
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        CookingStepNavigationResDto result = cookingRecordMapper.toCookingStepNavigationResDto(
                cookingSession,
                cookingStep
        );

        log.info(
                "[CookingRecordService] 다음 요리 단계 이동 종료 | moveToNextCookingStep() - END | cookingRecordId: {}, currentLevel: {}, nextLevel: {}",
                cookingRecordId,
                result.currentCookingStep().level(),
                result.nextCookingStepLevel()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자의 요리 세션을 이전 요리 단계로 이동합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 소유 요리 기록과 세션을 비관적 쓰기 잠금으로 조회합니다.
     * - 진행 중 세션의 현재 단계가 1이면 변경하지 않고 null을 반환합니다.
     * - 현재 단계가 2 이상이면 단계를 감소시키고 이전 단계 상세 정보를 반환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 이동한 요리 단계 정보, 현재 단계가 1이면 null
     */
    @Transactional
    public CookingStepNavigationResDto moveToPreviousCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        log.info(
                "[CookingRecordService] 이전 요리 단계 이동 시작 | moveToPreviousCookingStep() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdForUpdate(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));
        CookingSession cookingSession = cookingRecord.getCookingSession();
        if (cookingSession == null) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND);
        }
        if (cookingSession.getStatus() != CookingSessionStatus.IN_PROGRESS) {
            throw new CustomException(
                    CookingRecordErrorCode.COOKING_SESSION_NOT_IN_PROGRESS
            );
        }
        validateCookingStepState(cookingSession);

        if (cookingSession.isFirstStep()) {
            log.info(
                    "[CookingRecordService] 이전 요리 단계 이동 종료 | moveToPreviousCookingStep() - END | cookingRecordId: {}, currentLevel: 1",
                    cookingRecordId
            );
            return null;
        }

        cookingSession.moveToPreviousStep();
        CookingStep cookingStep = cookingStepRepository.findByCookingSessionIdAndLevel(
                        cookingSession.getId(),
                        cookingSession.getCurrentCookingStepLevel().longValue()
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        CookingStepNavigationResDto result =
                cookingRecordMapper.toCookingStepNavigationResDto(
                        cookingSession,
                        cookingStep
                );

        log.info(
                "[CookingRecordService] 이전 요리 단계 이동 종료 | moveToPreviousCookingStep() - END | cookingRecordId: {}, currentLevel: {}, prevLevel: {}",
                cookingRecordId,
                result.currentCookingStep().level(),
                result.prevCookingStepLevel()
        );
        return result;
    }

    /**
     * 요리 세션의 전체 단계 수와 현재 단계 번호가 이동 가능한 범위인지 검증합니다.
     *
     * @param cookingSession 검증할 요리 세션
     */
    private void validateCookingStepState(CookingSession cookingSession) {
        log.debug(
                "[CookingRecordService] 요리 단계 상태 검증 시작 | validateCookingStepState() - START | cookingSessionId: {}",
                cookingSession.getId()
        );
        if (cookingSession.getCookingStepCount() == null
                || cookingSession.getCookingStepCount() < 1
                || cookingSession.getCurrentCookingStepLevel() == null
                || cookingSession.getCurrentCookingStepLevel() < 1
                || cookingSession.getCurrentCookingStepLevel()
                        > cookingSession.getCookingStepCount()) {
            throw new CustomException(CookingRecordErrorCode.INVALID_COOKING_STEP_STATE);
        }
        log.debug(
                "[CookingRecordService] 요리 단계 상태 검증 종료 | validateCookingStepState() - END | currentLevel: {}, cookingStepCount: {}",
                cookingSession.getCurrentCookingStepLevel(),
                cookingSession.getCookingStepCount()
        );
    }
}
