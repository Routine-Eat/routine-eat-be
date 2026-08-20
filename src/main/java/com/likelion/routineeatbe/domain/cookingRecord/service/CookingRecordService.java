package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingStepGenerateGeminiResponseDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingRecordSearchResult;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingRecordSearchReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingSessionLogSearchReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingResultSaveReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingCompleteResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordInProgressResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordStepTitlesResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingSessionLogListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepMoveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CurrentCookingStepResDto;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingRecord;
import com.likelion.routineeatbe.domain.cookingRecord.entity.CookingStepFoodIngredient;
import com.likelion.routineeatbe.domain.cookingRecord.exception.CookingRecordErrorCode;
import com.likelion.routineeatbe.domain.cookingRecord.mapper.CookingRecordMapper;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingRecordRepository;
import com.likelion.routineeatbe.domain.cookingRecord.repository.CookingStepFoodIngredientRepository;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingStepGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionStatus;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSession;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSessionLog;
import com.likelion.routineeatbe.domain.cookingSession.entity.CookingStep;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingStepRepository;
import com.likelion.routineeatbe.domain.cookingSession.repository.CookingSessionLogRepository;
import com.likelion.routineeatbe.domain.cookingTip.entity.CookingStepTip;
import com.likelion.routineeatbe.domain.cookingTip.entity.CookingTip;
import com.likelion.routineeatbe.domain.cookingTip.repository.CookingStepTipRepository;
import com.likelion.routineeatbe.domain.cookingTip.repository.CookingTipRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.entity.RecipeStep;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeStepRepository;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.entity.RecipeFoodIngredient;
import com.likelion.routineeatbe.domain.recipeFoodIngredient.repository.RecipeFoodIngredientRepository;
import com.likelion.routineeatbe.domain.notification.entity.Notification;
import com.likelion.routineeatbe.domain.notification.enums.NotificationType;
import com.likelion.routineeatbe.domain.notification.service.NotificationService;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.domain.userStatistics.service.UserStatisticsService;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeFoodIngredientRepository recipeFoodIngredientRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final CookingSessionLogRepository cookingSessionLogRepository;
    private final CookingStepRepository cookingStepRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final CookingTipRepository cookingTipRepository;
    private final CookingStepTipRepository cookingStepTipRepository;
    private final CookingStepFoodIngredientRepository cookingStepFoodIngredientRepository;
    private final CookingStepGenerateGeminiService geminiService;
    private final CookingRecordPersistenceService persistenceService;
    private final CookingRecordImageStorageService imageStorageService;
    private final CookingRecordMapper cookingRecordMapper;
    private final UserStatisticsService userStatisticsService;
    private final NotificationService notificationService;

    /**
     * (1) 작업 목적
     * 사용자의 진행 중인 요리 세션에 연결된 요리 기록을 조회합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호로 사용자를 조회합니다.
     * - 사용자의 가장 최근 진행 중인 요리 기록을 조회합니다.
     * - 조회한 요리 기록을 응답 DTO로 변환합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @return 진행 중인 요리 기록 PK
     */
    @Transactional(readOnly = true)
    public CookingRecordInProgressResDto getInProgressCookingRecord(String userNumber) {
        log.info(
                "[CookingRecordService] 진행 중인 요리 세션 조회 시작 | getInProgressCookingRecord() - START | userNumber: {}",
                userNumber
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));

        /*
            2. 진행 중인 요리 기록 조회
            - 사용자의 가장 최근 IN_PROGRESS 상태 요리 기록이 없으면 예외를 발생시킵니다.
         */
        CookingRecord cookingRecord = cookingRecordRepository
                .findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                        user.getId(),
                        CookingSessionStatus.IN_PROGRESS
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND
                ));

        /*
            3. 진행 중인 요리 기록 응답 변환
            - 조회한 요리 기록을 Mapper로 응답 DTO로 변환합니다.
         */
        CookingRecordInProgressResDto result = cookingRecordMapper
                .toCookingRecordInProgressResDto(cookingRecord);

        log.info(
                "[CookingRecordService] 진행 중인 요리 세션 조회 종료 | getInProgressCookingRecord() - END | result: {}",
                result
        );
        return result;
    }

    /**
     * 진행 중인 요리 세션의 전체 단계 개수와 단계 제목을 조회합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @return 전체 요리 단계 개수와 단계 제목 목록
     */
    @Transactional(readOnly = true)
    public CookingRecordStepTitlesResDto getInProgressCookingStepTitles(String userNumber) {
        log.info(
                "[CookingRecordService] 진행 중인 요리 전체 단계 조회 시작 | "
                        + "getInProgressCookingStepTitles() - START | userNumber: {}",
                userNumber
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호로 사용자를 조회하고, 존재하지 않으면 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));

        /*
            2. 진행 중인 요리 기록 조회
            - 사용자의 가장 최근 진행 중인 요리 기록을 조회합니다.
         */
        CookingRecord cookingRecord = cookingRecordRepository
                .findFirstByUser_IdAndCookingSession_StatusOrderByCreatedAtDescIdDesc(
                        user.getId(),
                        CookingSessionStatus.IN_PROGRESS
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND
                ));

        /*
            3. 전체 요리 단계 조회
            - 진행 중인 요리 세션에 연결된 단계를 단계 번호 오름차순으로 조회합니다.
         */
        CookingSession cookingSession = cookingRecord.getCookingSession();
        List<CookingStep> cookingSteps = cookingStepRepository
                .findAllByCookingSessionIdAndLevelGreaterThanOrderByLevelAsc(cookingSession.getId(), 0L);

        /*
            4. Response DTO Mapping
            - 조회한 세션과 단계 목록을 전체 단계 제목 응답 DTO로 변환합니다.
         */
        CookingRecordStepTitlesResDto result = cookingRecordMapper
                .toCookingRecordStepTitlesResDto(cookingSession, cookingSteps);

        log.info(
                "[CookingRecordService] 진행 중인 요리 전체 단계 조회 종료 | "
                        + "getInProgressCookingStepTitles() - END | cookingStepCount: {}",
                result.cookingStepCount()
        );
        return result;
    }

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
     * 사용자 소유 요리 기록에 저장된 AI 대화 기록을 생성 순서대로 조회합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호와 요리 기록 PK로 소유권을 검증합니다.
     * - 연결된 요리 세션의 USER, AI, SYSTEM 로그를 위치 커서 기반으로 조회합니다.
     * - 다음 조회 위치를 계산하고 AI 대화 기록 목록 응답으로 변환합니다.
     *
     * @param cookingRecordId 조회할 요리 기록 PK
     * @param request 사용자 식별번호와 커서 조회 조건
     * @return AI 대화 기록 목록과 다음 커서 정보
     */
    @Transactional(readOnly = true)
    public CookingSessionLogListResDto getCookingSessionLogs(
            Long cookingRecordId,
            CookingSessionLogSearchReqDto request
    ) {
        log.info(
                "[CookingRecordService] AI 대화 기록 조회 시작 | getCookingSessionLogs() - START | cookingRecordId: {}, userNumber: {}, cursor: {}, size: {}",
                cookingRecordId,
                request.userNumber(),
                request.cursor(),
                request.size()
        );

        /*
            1. 사용자와 사용자 소유 요리 기록 조회
            - 사용자 고유 식별번호가 없거나 다른 사용자의 요리 기록이면 조회를 중단합니다.
         */
        User user = userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdWithCookingSession(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));

        /*
            2. 요리 세션과 대화 로그 조회
            - 세션 상태와 관계없이 연결된 세션의 전체 로그 타입을 생성 순서대로 조회합니다.
         */
        CookingSession cookingSession = cookingRecord.getCookingSession();
        if (cookingSession == null) {
            throw new CustomException(CookingRecordErrorCode.COOKING_SESSION_NOT_FOUND);
        }
        Slice<CookingSessionLog> cookingSessionLogSlice = cookingSessionLogRepository
                .searchByCookingSessionId(
                        cookingSession.getId(),
                        request.cursor(),
                        request.size()
                );

        /*
            3. 대화 기록 목록 응답 변환
            - 다음 데이터가 존재하면 다음 조회에 사용할 위치 커서를 계산합니다.
         */
        Integer nextCursor = cookingSessionLogSlice.hasNext()
                ? request.cursor() + request.size()
                : null;
        CookingSessionLogListResDto result = cookingRecordMapper
                .toCookingSessionLogListResDto(cookingSessionLogSlice, nextCursor);

        log.info(
                "[CookingRecordService] AI 대화 기록 조회 종료 | getCookingSessionLogs() - END | cookingRecordId: {}, resultSize: {}, nextCursor: {}",
                cookingRecordId,
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
     * - 오늘 세 번째 요리 결과 저장 차례인지 확인하고, 저장 트랜잭션 종료 후 사용자 통계와 리포트 도착 알림 생성을 비동기로 예약합니다.
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
        boolean shouldSaveUserStatistics = isThirdCookingResultToday(user.getId());

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

        if (shouldSaveUserStatistics) {
            userStatisticsService.saveUserStatistics(user)
                    .thenAccept(savedStatistics -> notificationService.save(Notification.create(
                            user,
                            NotificationType.THREE_MEAL_REPORT_ARRIVED,
                            savedStatistics.getId()
                    )));
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
     * 사용자의 오늘 요리 결과 저장 순서가 세 번째인지 확인합니다.
     *
     * @param userId 사용자 PK
     * @return 오늘 저장된 결과가 두 건이면 true
     */
    private boolean isThirdCookingResultToday(Long userId) {
        log.debug(
                "[CookingRecordService] 오늘 세 번째 요리 결과 여부 확인 시작 | isThirdCookingResultToday() - START | userId: {}",
                userId
        );
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);
        LocalDateTime startAt = today.atStartOfDay();
        LocalDateTime endAt = today.plusDays(1).atStartOfDay();
        long savedResultCount = cookingRecordRepository
                .countByUser_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndDifficultyLevelIsNotNull(
                        userId,
                        startAt,
                        endAt
                );
        boolean result = savedResultCount == 2;
        log.debug(
                "[CookingRecordService] 오늘 세 번째 요리 결과 여부 확인 종료 | isThirdCookingResultToday() - END | savedResultCount: {}, result: {}",
                savedResultCount,
                result
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자와 레시피 정보를 바탕으로 맞춤 요리 단계를 생성하고 요리를 시작합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자, 레시피, 재료와 기존 단계를 조회하고 중복 요리 시작 여부를 확인합니다.
     * - 전체 요리 팁을 Gemini에 전달해 단계별 관련 팁 PK를 포함한 요리 단계를 생성합니다.
     * - 단계별 사용 음식 재료 PK를 생성해 요리 기록 음식 재료와 함께 저장합니다.
     * - 저장된 1번 단계의 요리 팁과 음식 재료 상세 정보를 요리 시작 응답 DTO로 변환합니다.
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
        List<CookingTip> cookingTips = cookingTipRepository.findAllByOrderByIdAsc();
        if (cookingTips.isEmpty()) {
            throw new CustomException(CookingRecordErrorCode.COOKING_TIP_EMPTY);
        }

        CookingStepGenerateGeminiResponseDto generated = geminiService.generate(
                user,
                recipe,
                ingredients,
                recipeSteps,
                cookingTips,
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
        List<CookingStepTip> firstCookingStepTips = cookingStepTipRepository
                .findAllWithCookingTipAndContentsByCookingStepId(firstCookingStep.getId());
        List<CookingStepFoodIngredient> firstCookingStepFoodIngredients =
                cookingStepFoodIngredientRepository
                        .findAllWithCookingRecordFoodIngredientByCookingStepId(
                                firstCookingStep.getId()
                        );
        CookingStartResDto result = cookingRecordMapper.toCookingStartResDto(
                cookingRecord,
                recipe,
                generated,
                firstCookingStep,
                firstCookingStepTips,
                firstCookingStepFoodIngredients
        );

        log.info(
                "[CookingRecordService] 요리 시작 종료 | startCooking() - END | cookingRecordId: {}",
                result.cookingRecordId()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자의 진행 중인 요리 세션에서 현재 요리 단계 정보를 조회합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호로 사용자를 조회합니다.
     * - 사용자 소유 요리 기록과 요리 세션을 조회하고 진행 중 상태를 검증합니다.
     * - 현재 단계의 요리 팁과 음식 재료를 조회해 응답 DTO로 변환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 현재 요리 단계 상세 정보
     */
    @Transactional(readOnly = true)
    public CurrentCookingStepResDto getCurrentCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        log.info(
                "[CookingRecordService] 현재 요리 단계 조회 시작 | getCurrentCookingStep() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        /*
            1. 사용자 및 요리 기록 조회
            - 사용자 고유 식별번호와 사용자 소유 요리 기록이 존재하지 않으면 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdWithCookingSession(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));

        /*
            2. 요리 세션 및 현재 단계 상태 검증
            - 요리 세션 존재 여부, 진행 상태와 현재 단계 번호의 유효 범위를 검증합니다.
         */
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

        /*
            3. 현재 요리 단계와 연관 정보 조회
            - 현재 단계 번호로 요리 단계를 조회하고 연결된 요리 팁과 음식 재료를 조회합니다.
         */
        CookingStep cookingStep = cookingStepRepository.findByCookingSessionIdAndLevel(
                        cookingSession.getId(),
                        cookingSession.getCurrentCookingStepLevel().longValue()
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        List<CookingStepTip> cookingStepTips = cookingStepTipRepository
                .findAllWithCookingTipAndContentsByCookingStepId(cookingStep.getId());
        List<CookingStepFoodIngredient> cookingStepFoodIngredients =
                cookingStepFoodIngredientRepository
                        .findAllWithCookingRecordFoodIngredientByCookingStepId(
                                cookingStep.getId()
                        );

        /*
            4. 현재 요리 단계 응답 변환
            - 요리 세션과 현재 단계 연관 정보를 응답 DTO로 변환합니다.
         */
        CurrentCookingStepResDto result = cookingRecordMapper.toCurrentCookingStepResDto(
                cookingSession,
                cookingStep,
                cookingStepTips,
                cookingStepFoodIngredients
        );

        log.info(
                "[CookingRecordService] 현재 요리 단계 조회 종료 | getCurrentCookingStep() - END | cookingRecordId: {}, currentLevel: {}",
                cookingRecordId,
                result.currentCookingStep().level()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자의 진행 중인 요리 세션을 마지막 요리 단계로 변경합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 소유 요리 기록과 세션을 비관적 쓰기 잠금으로 조회합니다.
     * - 진행 중 세션의 마지막 단계 번호로 현재 단계를 변경합니다.
     * - 마지막 단계의 요리 팁과 음식 재료를 조회해 응답 DTO로 변환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 마지막 요리 단계 상세 정보
     */
    @Transactional
    public CurrentCookingStepResDto moveToLastCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        log.info(
                "[CookingRecordService] 마지막 요리 단계 이동 시작 | moveToLastCookingStep() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        /*
            1. 사용자 및 요리 기록 조회
            - 사용자와 사용자 소유 요리 기록을 조회하고 동시 변경을 방지하기 위해 잠금을 적용합니다.
         */
        User user = userRepository.findByLoginNumber(userNumber)
                .orElseThrow(() -> new CustomException(CookingRecordErrorCode.USER_NOT_FOUND));
        CookingRecord cookingRecord = cookingRecordRepository
                .findByIdAndUserIdForUpdate(cookingRecordId, user.getId())
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_RECORD_NOT_FOUND
                ));

        /*
            2. 요리 세션 상태 검증 및 마지막 단계 이동
            - 요리 세션 존재 여부와 진행 상태를 검증한 뒤 마지막 단계로 변경합니다.
         */
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
        Integer lastCookingStepLevel = cookingSession.getCookingStepCount();
        cookingSession.moveToStep(lastCookingStepLevel);

        /*
            3. 마지막 요리 단계와 연관 정보 조회
            - 마지막 단계와 연결된 요리 팁 및 음식 재료를 조회합니다.
         */
        CookingStep cookingStep = cookingStepRepository.findByCookingSessionIdAndLevel(
                        cookingSession.getId(),
                        lastCookingStepLevel.longValue()
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        List<CookingStepTip> cookingStepTips = cookingStepTipRepository
                .findAllWithCookingTipAndContentsByCookingStepId(cookingStep.getId());
        List<CookingStepFoodIngredient> cookingStepFoodIngredients =
                cookingStepFoodIngredientRepository
                        .findAllWithCookingRecordFoodIngredientByCookingStepId(
                                cookingStep.getId()
                        );

        /*
            4. 마지막 요리 단계 응답 변환
            - 변경된 요리 세션과 마지막 단계 연관 정보를 응답 DTO로 변환합니다.
         */
        CurrentCookingStepResDto result = cookingRecordMapper.toCurrentCookingStepResDto(
                cookingSession,
                cookingStep,
                cookingStepTips,
                cookingStepFoodIngredients
        );

        log.info(
                "[CookingRecordService] 마지막 요리 단계 이동 종료 | moveToLastCookingStep() - END | cookingRecordId: {}, currentLevel: {}",
                cookingRecordId,
                result.currentCookingStep().level()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 사용자의 요리 세션을 다음 요리 단계로 이동하거나 마지막 단계에서 완료 처리합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 소유 요리 기록과 세션을 비관적 쓰기 잠금으로 조회합니다.
     * - 진행 중 세션의 현재 단계가 마지막이면 완료 상태로 변경하고 메뉴명과 완료 날짜를 반환합니다.
     * - 마지막 단계가 아니면 현재 단계를 증가시키고 요리 팁과 사용 음식 재료를 포함한 다음 단계 상세 정보를 반환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @return 이동한 요리 단계 정보 또는 요리 완료 정보
     */
    @Transactional
    public CookingStepMoveResDto moveToNextCookingStep(
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
            CookingCompleteResDto result = cookingRecordMapper.toCookingCompleteResDto(
                    cookingRecord,
                    LocalDate.now()
            );
            log.info(
                    "[CookingRecordService] 다음 요리 단계 이동 종료 | moveToNextCookingStep() - END | cookingRecordId: {}, status: {}, cookedDate: {}",
                    cookingRecordId,
                    cookingSession.getStatus(),
                    result.cookedDate()
            );
            return result;
        }

        cookingSession.moveToNextStep();
        CookingStep cookingStep = cookingStepRepository.findByCookingSessionIdAndLevel(
                        cookingSession.getId(),
                        cookingSession.getCurrentCookingStepLevel().longValue()
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        List<CookingStepTip> cookingStepTips = cookingStepTipRepository
                .findAllWithCookingTipAndContentsByCookingStepId(cookingStep.getId());
        List<CookingStepFoodIngredient> cookingStepFoodIngredients =
                cookingStepFoodIngredientRepository
                        .findAllWithCookingRecordFoodIngredientByCookingStepId(
                                cookingStep.getId()
                        );
        CookingStepNavigationResDto result = cookingRecordMapper.toCookingStepNavigationResDto(
                cookingSession,
                cookingStep,
                cookingStepTips,
                cookingStepFoodIngredients
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
     * - 현재 단계가 2 이상이면 단계를 감소시키고 요리 팁과 사용 음식 재료를 포함한 이전 단계 상세 정보를 반환합니다.
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
        List<CookingStepTip> cookingStepTips = cookingStepTipRepository
                .findAllWithCookingTipAndContentsByCookingStepId(cookingStep.getId());
        List<CookingStepFoodIngredient> cookingStepFoodIngredients =
                cookingStepFoodIngredientRepository
                        .findAllWithCookingRecordFoodIngredientByCookingStepId(
                                cookingStep.getId()
                        );
        CookingStepNavigationResDto result =
                cookingRecordMapper.toCookingStepNavigationResDto(
                        cookingSession,
                        cookingStep,
                        cookingStepTips,
                        cookingStepFoodIngredients
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
     * (1) 작업 목적
     * 사용자의 요리 세션을 지정한 요리 단계로 이동합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 소유 요리 기록과 세션을 비관적 쓰기 잠금으로 조회합니다.
     * - 진행 중 세션과 요청 단계 번호의 유효 범위를 검증합니다.
     * - 현재 단계를 요청 단계로 변경하고 요리 팁과 사용 음식 재료를 포함한 단계 상세 정보를 반환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @param targetLevel 이동할 요리 단계 번호
     * @return 이동한 요리 단계 정보
     */
    @Transactional
    public CookingStepNavigationResDto moveToCookingStep(
            Long cookingRecordId,
            String userNumber,
            Integer targetLevel
    ) {
        log.info(
                "[CookingRecordService] 특정 요리 단계 이동 시작 | moveToCookingStep() - START | cookingRecordId: {}, userNumber: {}, targetLevel: {}",
                cookingRecordId,
                userNumber,
                targetLevel
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
        if (targetLevel == null
                || targetLevel < 1
                || targetLevel > cookingSession.getCookingStepCount()) {
            throw new CustomException(CookingRecordErrorCode.INVALID_COOKING_STEP_LEVEL);
        }

        cookingSession.moveToStep(targetLevel);
        CookingStep cookingStep = cookingStepRepository.findByCookingSessionIdAndLevel(
                        cookingSession.getId(),
                        targetLevel.longValue()
                )
                .orElseThrow(() -> new CustomException(
                        CookingRecordErrorCode.COOKING_STEP_NOT_FOUND
                ));
        List<CookingStepTip> cookingStepTips = cookingStepTipRepository
                .findAllWithCookingTipAndContentsByCookingStepId(cookingStep.getId());
        List<CookingStepFoodIngredient> cookingStepFoodIngredients =
                cookingStepFoodIngredientRepository
                        .findAllWithCookingRecordFoodIngredientByCookingStepId(
                                cookingStep.getId()
                        );
        CookingStepNavigationResDto result = cookingRecordMapper.toCookingStepNavigationResDto(
                cookingSession,
                cookingStep,
                cookingStepTips,
                cookingStepFoodIngredients
        );

        log.info(
                "[CookingRecordService] 특정 요리 단계 이동 종료 | moveToCookingStep() - END | cookingRecordId: {}, currentLevel: {}",
                cookingRecordId,
                result.currentCookingStep().level()
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
