package com.likelion.routineeatbe.domain.cookingRecord.service;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiContextDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiResult;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingAiGeminiCallDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingAiGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingAiReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingAiAnswerResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingAiGeminiService;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingSpeechGenerateGeminiService;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookingAiService {

    private final CookingAiContextService cookingAiContextService;
    private final CookingRecordService cookingRecordService;
    private final CookingAiGeminiService cookingAiGeminiService;
    private final CookingSpeechGenerateGeminiService cookingSpeechGenerateGeminiService;

    /**
     * (1) 작업 목적
     * 요리 중 사용자 발화를 시스템 동작 또는 AI 질문으로 처리합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 발화를 로그에 저장하고 현재 요리 컨텍스트를 조회합니다.
     * - Gemini Tool Call 결과에 따라 단계 이동 또는 요리 답변 생성을 실행합니다.
     * - 텍스트 답변을 AI 로그에 저장한 뒤 WAV 음성으로 변환합니다.
     *
     * @param cookingRecordId 요리 기록 PK
     * @param userNumber 사용자 고유 식별번호
     * @param request 사용자 발화 요청
     * @return 응답 메시지, JSON 데이터와 선택 음성 바이너리
     */
    public CookingAiResult interact(
            Long cookingRecordId,
            String userNumber,
            CookingAiReqDto request
    ) {
        log.info(
                "[CookingAiService] 요리 중 AI 상호작용 시작 | interact() - START | cookingRecordId: {}, userNumber: {}",
                cookingRecordId,
                userNumber
        );

        CookingAiContextDto context = cookingAiContextService.prepareContext(
                cookingRecordId,
                userNumber,
                request.userSpeechText()
        );
        CookingAiGeminiCallDto call = cookingAiGeminiService.startInteraction(
                context,
                request.userSpeechText()
        );
        CookingAiResult result = dispatch(
                call,
                context,
                userNumber,
                request.userSpeechText()
        );

        log.info(
                "[CookingAiService] 요리 중 AI 상호작용 종료 | interact() - END | cookingRecordId: {}, hasAudio: {}",
                cookingRecordId,
                result.audio() != null
        );
        return result;
    }

    /**
     * Gemini가 선택한 Function Call을 실제 시스템 동작 또는 답변 생성으로 분기합니다.
     *
     * @param call Gemini Function Call 정보
     * @param context 현재 요리 컨텍스트
     * @param userNumber 사용자 고유 식별번호
     * @param userSpeechText 사용자 발화
     * @return 처리 완료 결과
     */
    private CookingAiResult dispatch(
            CookingAiGeminiCallDto call,
            CookingAiContextDto context,
            String userNumber,
            String userSpeechText
    ) {
        log.debug(
                "[CookingAiService] Gemini Function 분기 시작 | dispatch() - START | functionName: {}",
                call.functionName()
        );
        CookingAiResult result = switch (call.functionName()) {
            case CookingAiGeminiFunctionDeclarationDto.MOVE_NEXT_FUNCTION_NAME ->
                    moveToNextCookingStep(call, context, userNumber);
            case CookingAiGeminiFunctionDeclarationDto.MOVE_PREVIOUS_FUNCTION_NAME ->
                    moveToPreviousCookingStep(call, context, userNumber);
            case CookingAiGeminiFunctionDeclarationDto.MOVE_TO_FUNCTION_NAME ->
                    moveToSpecificCookingStep(call, context, userNumber);
            case CookingAiGeminiFunctionDeclarationDto.REQUEST_CONTEXT_FUNCTION_NAME ->
                    generateCookingAnswer(call, context, userSpeechText);
            default -> throw new CustomException(GeminiErrorCode.INVALID_FUNCTION_CALL);
        };
        log.debug(
                "[CookingAiService] Gemini Function 분기 종료 | dispatch() - END | functionName: {}",
                call.functionName()
        );
        return result;
    }

    /**
     * 다음 요리 단계로 이동하고 시스템 동작 결과를 생성합니다.
     *
     * @param call Gemini Function Call 정보
     * @param context 현재 요리 컨텍스트
     * @param userNumber 사용자 고유 식별번호
     * @return 다음 단계 이동 또는 요리 종료 결과
     */
    private CookingAiResult moveToNextCookingStep(
            CookingAiGeminiCallDto call,
            CookingAiContextDto context,
            String userNumber
    ) {
        log.debug(
                "[CookingAiService] 다음 요리 단계 이동 시작 | moveToNextCookingStep() - START | cookingRecordId: {}",
                context.cookingRecordId()
        );
        CookingStepNavigationResDto navigation = cookingRecordService.moveToNextCookingStep(
                context.cookingRecordId(),
                userNumber
        );
        String message = navigation == null
                ? "요리가 종료되었습니다."
                : "다음 요리 단계로 이동했습니다. 현재 %d번째 단계입니다."
                        .formatted(navigation.currentCookingStep().level());
        CookingAiResult result = completeSystemCommand(call, context, message, navigation);
        log.debug(
                "[CookingAiService] 다음 요리 단계 이동 종료 | moveToNextCookingStep() - END | cookingRecordId: {}",
                context.cookingRecordId()
        );
        return result;
    }

    /**
     * 이전 요리 단계로 이동하고 시스템 동작 결과를 생성합니다.
     *
     * @param call Gemini Function Call 정보
     * @param context 현재 요리 컨텍스트
     * @param userNumber 사용자 고유 식별번호
     * @return 이전 단계 이동 결과
     */
    private CookingAiResult moveToPreviousCookingStep(
            CookingAiGeminiCallDto call,
            CookingAiContextDto context,
            String userNumber
    ) {
        log.debug(
                "[CookingAiService] 이전 요리 단계 이동 시작 | moveToPreviousCookingStep() - START | cookingRecordId: {}",
                context.cookingRecordId()
        );
        CookingStepNavigationResDto navigation = cookingRecordService
                .moveToPreviousCookingStep(context.cookingRecordId(), userNumber);
        String message = navigation == null
                ? "첫 번째 단계 이전으로 이동할 수 없습니다."
                : "이전 요리 단계로 이동했습니다. 현재 %d번째 단계입니다."
                        .formatted(navigation.currentCookingStep().level());
        CookingAiResult result = completeSystemCommand(call, context, message, navigation);
        log.debug(
                "[CookingAiService] 이전 요리 단계 이동 종료 | moveToPreviousCookingStep() - END | cookingRecordId: {}",
                context.cookingRecordId()
        );
        return result;
    }

    /**
     * Gemini가 지정한 요리 단계로 이동하고 시스템 동작 결과를 생성합니다.
     *
     * @param call Gemini Function Call 정보
     * @param context 현재 요리 컨텍스트
     * @param userNumber 사용자 고유 식별번호
     * @return 특정 단계 이동 결과
     */
    private CookingAiResult moveToSpecificCookingStep(
            CookingAiGeminiCallDto call,
            CookingAiContextDto context,
            String userNumber
    ) {
        log.debug(
                "[CookingAiService] 특정 요리 단계 이동 시작 | moveToSpecificCookingStep() - START | cookingRecordId: {}",
                context.cookingRecordId()
        );
        JsonLevelArguments arguments = convertLevelArguments(call);
        CookingStepNavigationResDto navigation = cookingRecordService.moveToCookingStep(
                context.cookingRecordId(),
                userNumber,
                arguments.level()
        );
        String message = "요리 단계로 이동했습니다. 현재 %d번째 단계입니다."
                .formatted(navigation.currentCookingStep().level());
        CookingAiResult result = completeSystemCommand(call, context, message, navigation);
        log.debug(
                "[CookingAiService] 특정 요리 단계 이동 종료 | moveToSpecificCookingStep() - END | cookingRecordId: {}, level: {}",
                context.cookingRecordId(),
                arguments.level()
        );
        return result;
    }

    /**
     * 시스템 동작 로그를 저장하고 Gemini에 Function Result를 전달합니다.
     *
     * @param call Gemini Function Call 정보
     * @param context 현재 요리 컨텍스트
     * @param message 시스템 동작 결과 메시지
     * @param navigation 요리 단계 이동 결과
     * @return 음성이 없는 시스템 동작 응답
     */
    private CookingAiResult completeSystemCommand(
            CookingAiGeminiCallDto call,
            CookingAiContextDto context,
            String message,
            CookingStepNavigationResDto navigation
    ) {
        log.debug(
                "[CookingAiService] 시스템 동작 완료 처리 시작 | completeSystemCommand() - START | cookingSessionId: {}",
                context.cookingSessionId()
        );
        cookingAiContextService.saveSystemLog(context.cookingSessionId(), message);
        cookingAiGeminiService.completeSystemCommand(call, message);
        CookingAiResult result = CookingAiResult.create(message, navigation, null);
        log.debug(
                "[CookingAiService] 시스템 동작 완료 처리 종료 | completeSystemCommand() - END | cookingSessionId: {}",
                context.cookingSessionId()
        );
        return result;
    }

    /**
     * 현재 요리 컨텍스트 기반 답변을 생성하고 AI 로그와 음성을 생성합니다.
     *
     * @param call Gemini Function Call 정보
     * @param context 현재 요리 컨텍스트
     * @param userSpeechText 사용자 발화
     * @return 텍스트 답변과 WAV 음성 응답
     */
    private CookingAiResult generateCookingAnswer(
            CookingAiGeminiCallDto call,
            CookingAiContextDto context,
            String userSpeechText
    ) {
        log.debug(
                "[CookingAiService] 요리 답변 및 음성 생성 시작 | generateCookingAnswer() - START | cookingSessionId: {}",
                context.cookingSessionId()
        );
        String answer = cookingAiGeminiService.generateAnswer(call, context, userSpeechText);
        cookingAiContextService.saveAiLog(context.cookingSessionId(), answer);
        byte[] audio = cookingSpeechGenerateGeminiService.generate(answer);
        CookingAiResult result = CookingAiResult.create(
                "응답이 반환되었습니다.",
                CookingAiAnswerResDto.create(answer),
                audio
        );
        log.debug(
                "[CookingAiService] 요리 답변 및 음성 생성 종료 | generateCookingAnswer() - END | cookingSessionId: {}, audioSize: {}",
                context.cookingSessionId(),
                audio.length
        );
        return result;
    }

    /**
     * 특정 단계 이동 Function Call에서 유효한 단계 번호를 추출합니다.
     *
     * @param call Gemini Function Call 정보
     * @return 검증된 요리 단계 인자
     */
    private JsonLevelArguments convertLevelArguments(CookingAiGeminiCallDto call) {
        log.debug(
                "[CookingAiService] 특정 단계 인자 변환 시작 | convertLevelArguments() - START"
        );
        int level = call.arguments().path("level").asInt(0);
        if (level < 1) {
            throw new CustomException(GeminiErrorCode.INVALID_FUNCTION_CALL);
        }
        JsonLevelArguments result = new JsonLevelArguments(level);
        log.debug(
                "[CookingAiService] 특정 단계 인자 변환 종료 | convertLevelArguments() - END | level: {}",
                level
        );
        return result;
    }

    private record JsonLevelArguments(Integer level) {
    }
}
