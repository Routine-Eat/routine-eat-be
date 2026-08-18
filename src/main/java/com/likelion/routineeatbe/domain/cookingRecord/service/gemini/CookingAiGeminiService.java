package com.likelion.routineeatbe.domain.cookingRecord.service.gemini;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiContextDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiContextDto.CookingStepContext;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiContextDto.FoodIngredientContext;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingAiAnswerGeminiDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingAiGeminiCallDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingAiGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingAiInteractionReqDto;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

@Slf4j
@Service
public class CookingAiGeminiService {

    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final String FUNCTION_CALL_TYPE = "function_call";
    private static final String IRRELEVANT_ANSWER =
            "죄송합니다. 요리와 관련없는 질문이나 말에는 응답할 수 없습니다.";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeminiProperties properties;

    public CookingAiGeminiService(
            @Qualifier("geminiRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            GeminiProperties properties
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * (1) 작업 목적
     * 사용자 발화가 시스템 동작 명령인지 일반 질문인지 Gemini Tool Call로 판별합니다.
     *
     * (2) 세부 작업 내용
     * - 현재 요리 단계 목록과 사용자 발화를 명령 판별 프롬프트에 포함합니다.
     * - 다음, 이전, 특정 단계 이동 또는 요리 컨텍스트 요청 Tool 중 하나를 강제로 호출합니다.
     *
     * @param context 현재 진행 중인 요리 컨텍스트
     * @param userSpeechText 사용자의 발화 텍스트
     * @return Gemini가 선택한 Function Call 정보
     */
    public CookingAiGeminiCallDto startInteraction(
            CookingAiContextDto context,
            String userSpeechText
    ) {
        log.info(
                "[CookingAiGeminiService] 요리 발화 명령 판별 시작 | startInteraction() - START | cookingRecordId: {}",
                context.cookingRecordId()
        );

        String prompt = createCommandPrompt(context, userSpeechText);
        CookingAiInteractionReqDto request = CookingAiInteractionReqDto.createFunctionCall(
                properties.cookingGenerateModel(),
                prompt,
                CookingAiGeminiFunctionDeclarationDto.createCommandTools()
        );
        JsonNode response = postInteraction(request);
        CookingAiGeminiCallDto result = parseFunctionCall(prompt, response);

        log.info(
                "[CookingAiGeminiService] 요리 발화 명령 판별 종료 | startInteraction() - END | functionName: {}",
                result.functionName()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * 메뉴, 요리 단계와 음식 재료 정보를 기반으로 사용자 질문에 답변합니다.
     *
     * (2) 세부 작업 내용
     * - 최초 Tool Call에 요리 설명 프롬프트를 Function Result로 전달합니다.
     * - 최종 답변 Tool Call을 파싱하고 요리와 무관한 발화는 고정 문구로 정규화합니다.
     *
     * @param call 최초 Gemini Function Call 정보
     * @param context 현재 진행 중인 요리 컨텍스트
     * @param userSpeechText 사용자의 발화 텍스트
     * @return 사용자에게 전달할 최종 텍스트 답변
     */
    public String generateAnswer(
            CookingAiGeminiCallDto call,
            CookingAiContextDto context,
            String userSpeechText
    ) {
        log.info(
                "[CookingAiGeminiService] 요리 질문 답변 생성 시작 | generateAnswer() - START | cookingRecordId: {}",
                context.cookingRecordId()
        );

        String cookingPrompt = createCookingDescriptionPrompt(context, userSpeechText);
        ArrayNode input = createContinuationInput(call, cookingPrompt);
        CookingAiInteractionReqDto request = CookingAiInteractionReqDto.createFunctionCall(
                properties.cookingGenerateModel(),
                input,
                List.of(CookingAiGeminiFunctionDeclarationDto.createAnswerTool())
        );
        CookingAiGeminiCallDto answerCall = parseFunctionCall(
                call.initialPrompt(),
                postInteraction(request)
        );
        if (!CookingAiGeminiFunctionDeclarationDto.RETURN_ANSWER_FUNCTION_NAME.equals(
                answerCall.functionName()
        )) {
            throw new CustomException(GeminiErrorCode.INVALID_FUNCTION_CALL);
        }
        CookingAiAnswerGeminiDto generated = convertArguments(
                answerCall.arguments(),
                CookingAiAnswerGeminiDto.class
        );
        String result = normalizeAnswer(generated);

        log.info(
                "[CookingAiGeminiService] 요리 질문 답변 생성 종료 | generateAnswer() - END | answerLength: {}",
                result.length()
        );
        return result;
    }

    /**
     * (1) 작업 목적
     * Spring Boot에서 실행한 시스템 동작 결과를 Gemini에 전달합니다.
     *
     * (2) 세부 작업 내용
     * - 최초 Function Call과 실행 결과를 stateless 대화 이력으로 구성합니다.
     * - 모델 출력은 최소화하고 후속 응답은 사용하지 않습니다.
     * - 전달 실패는 이미 완료된 시스템 동작을 재실행하지 않도록 경고 로그만 남깁니다.
     *
     * @param call 최초 Gemini Function Call 정보
     * @param systemResult 시스템 동작 실행 결과
     */
    public void completeSystemCommand(CookingAiGeminiCallDto call, String systemResult) {
        log.info(
                "[CookingAiGeminiService] 시스템 동작 결과 전달 시작 | completeSystemCommand() - START | functionName: {}",
                call.functionName()
        );
        try {
            ArrayNode input = createContinuationInput(call, systemResult);
            postInteraction(CookingAiInteractionReqDto.createNoOutput(
                    properties.cookingGenerateModel(),
                    input
            ));
        } catch (CustomException exception) {
            log.warn(
                    "[CookingAiGeminiService] 시스템 동작 결과 전달 실패 | completeSystemCommand() | functionName: {}, errorCode: {}",
                    call.functionName(),
                    exception.getErrorCode().getCode()
            );
        }
        log.info(
                "[CookingAiGeminiService] 시스템 동작 결과 전달 종료 | completeSystemCommand() - END | functionName: {}",
                call.functionName()
        );
    }

    /**
     * Gemini 명령 판별 프롬프트를 생성합니다.
     *
     * @param context 현재 진행 중인 요리 컨텍스트
     * @param userSpeechText 사용자 발화
     * @return 명령 판별 프롬프트
     */
    private String createCommandPrompt(CookingAiContextDto context, String userSpeechText) {
        log.debug(
                "[CookingAiGeminiService] 명령 판별 프롬프트 생성 시작 | createCommandPrompt() - START | cookingRecordId: {}",
                context.cookingRecordId()
        );
        StringBuilder prompt = new StringBuilder("""
                사용자의 발화를 분석하여 반드시 제공된 함수 중 하나만 호출하세요.
                단계 이동, 완료, 되돌리기 또는 특정 단계 요청이면 해당 시스템 함수를 호출하세요.
                질문, 감탄, 상태 설명 또는 현재 요리와 무관한 발화라면 request_cooking_context를 호출하세요.
                현재 단계는 %d이고 전체 단계 수는 %d입니다.

                [요리 단계]
                """.formatted(
                context.currentCookingStepLevel(),
                context.cookingStepCount()
        ));
        context.cookingSteps().forEach(step -> prompt.append("- level=")
                .append(step.level())
                .append(", title=")
                .append(step.title())
                .append(System.lineSeparator()));
        prompt.append("\n[사용자 발화]\n").append(userSpeechText);
        String result = prompt.toString();
        log.debug(
                "[CookingAiGeminiService] 명령 판별 프롬프트 생성 종료 | createCommandPrompt() - END | promptLength: {}",
                result.length()
        );
        return result;
    }

    /**
     * 메뉴와 현재 요리 정보를 포함한 답변 생성 프롬프트를 생성합니다.
     *
     * @param context 현재 진행 중인 요리 컨텍스트
     * @param userSpeechText 사용자 발화
     * @return 요리 답변 생성 프롬프트
     */
    private String createCookingDescriptionPrompt(
            CookingAiContextDto context,
            String userSpeechText
    ) {
        log.debug(
                "[CookingAiGeminiService] 메뉴 요리 설명 프롬프트 생성 시작 | createCookingDescriptionPrompt() - START | cookingRecordId: {}",
                context.cookingRecordId()
        );
        StringBuilder prompt = new StringBuilder("""
                아래 메뉴, 요리 단계와 재료 정보만 근거로 사용자의 질문에 짧은 한국어 구어체로 답하세요.
                사용자가 바로 들을 음성 문장이므로 Markdown, 목록 기호와 불필요한 서론을 사용하지 마세요.
                안전에 영향을 주는 질문은 보수적으로 안내하세요.
                현재 진행 중인 요리와 관련없는 질문이나 말이면 cookingRelated=false로 설정하고
                answer는 정확히 '%s'로 반환하세요.

                [메뉴]
                name=%s, type=%s, difficulty=%s, timeRequiredMinutes=%d, calory=%s, servings=%d

                [현재 상태]
                currentLevel=%d, cookingStepCount=%d

                [요리 단계]
                """.formatted(
                IRRELEVANT_ANSWER,
                context.menuName(),
                context.menuType(),
                context.difficultyLevel(),
                context.timeRequired(),
                context.calory(),
                context.servings(),
                context.currentCookingStepLevel(),
                context.cookingStepCount()
        ));
        for (CookingStepContext step : context.cookingSteps()) {
            prompt.append("- level=").append(step.level())
                    .append(", title=").append(step.title())
                    .append(", content=").append(step.content())
                    .append(", subContent=").append(step.subContent())
                    .append(System.lineSeparator());
        }
        prompt.append("\n[재료]\n");
        for (FoodIngredientContext ingredient : context.foodIngredients()) {
            prompt.append("- name=").append(ingredient.name())
                    .append(", primaryAmount=").append(ingredient.primaryAmount())
                    .append(ingredient.primaryUnit());
            if (ingredient.secondaryAmount() != null) {
                prompt.append(", secondaryAmount=").append(ingredient.secondaryAmount())
                        .append(ingredient.secondaryUnit());
            }
            prompt.append(System.lineSeparator());
        }
        prompt.append("\n[사용자 발화]\n").append(userSpeechText);
        String result = prompt.toString();
        log.debug(
                "[CookingAiGeminiService] 메뉴 요리 설명 프롬프트 생성 종료 | createCookingDescriptionPrompt() - END | promptLength: {}",
                result.length()
        );
        return result;
    }

    /**
     * 최초 사용자 입력, Function Call과 Function Result를 후속 요청 입력으로 구성합니다.
     *
     * @param call 최초 Gemini Function Call 정보
     * @param resultText Function Result로 전달할 텍스트
     * @return stateless 후속 요청 입력
     */
    private ArrayNode createContinuationInput(CookingAiGeminiCallDto call, String resultText) {
        log.debug(
                "[CookingAiGeminiService] 후속 요청 입력 생성 시작 | createContinuationInput() - START | functionName: {}",
                call.functionName()
        );
        ArrayNode input = objectMapper.createArrayNode();
        input.add(objectMapper.valueToTree(Map.of(
                "type", "user_input",
                "content", List.of(Map.of("type", "text", "text", call.initialPrompt()))
        )));
        JsonNode steps = call.interactionResponse().path("steps");
        if (!steps.isArray()) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }
        for (JsonNode step : steps) {
            input.add(step);
        }
        input.add(objectMapper.valueToTree(Map.of(
                "type", "function_result",
                "name", call.functionName(),
                "call_id", call.callId(),
                "result", List.of(Map.of("type", "text", "text", resultText))
        )));
        log.debug(
                "[CookingAiGeminiService] 후속 요청 입력 생성 종료 | createContinuationInput() - END | inputSize: {}",
                input.size()
        );
        return input;
    }

    /**
     * Gemini 응답에서 Function Call을 추출하고 필수 필드를 검증합니다.
     *
     * @param prompt 최초 사용자 프롬프트
     * @param response Gemini 응답 JSON
     * @return 검증된 Function Call 정보
     */
    private CookingAiGeminiCallDto parseFunctionCall(String prompt, JsonNode response) {
        log.debug(
                "[CookingAiGeminiService] Function Call 파싱 시작 | parseFunctionCall() - START"
        );
        if (response == null || !response.path("steps").isArray()) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }
        for (JsonNode step : response.path("steps")) {
            if (FUNCTION_CALL_TYPE.equals(step.path("type").asText())) {
                String callId = step.path("id").asText();
                String functionName = step.path("name").asText();
                JsonNode arguments = step.path("arguments");
                if (callId.isBlank() || functionName.isBlank() || arguments.isMissingNode()) {
                    throw new CustomException(GeminiErrorCode.INVALID_FUNCTION_CALL);
                }
                CookingAiGeminiCallDto result = CookingAiGeminiCallDto.create(
                        prompt,
                        response,
                        callId,
                        functionName,
                        arguments
                );
                log.debug(
                        "[CookingAiGeminiService] Function Call 파싱 종료 | parseFunctionCall() - END | functionName: {}",
                        functionName
                );
                return result;
            }
        }
        throw new CustomException(GeminiErrorCode.INVALID_FUNCTION_CALL);
    }

    /**
     * Function Call 인자를 지정한 DTO로 변환합니다.
     *
     * @param arguments Function Call 인자 JSON
     * @param responseType 변환 대상 타입
     * @param <T> 변환 대상 타입
     * @return 변환된 DTO
     */
    private <T> T convertArguments(JsonNode arguments, Class<T> responseType) {
        log.debug(
                "[CookingAiGeminiService] Function 인자 변환 시작 | convertArguments() - START | responseType: {}",
                responseType.getSimpleName()
        );
        try {
            T result = objectMapper.treeToValue(arguments, responseType);
            log.debug(
                    "[CookingAiGeminiService] Function 인자 변환 종료 | convertArguments() - END | responseType: {}",
                    responseType.getSimpleName()
            );
            return result;
        } catch (JacksonException exception) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }
    }

    /**
     * 요리 관련 여부와 답변 값을 검증하고 최종 응답 문구를 반환합니다.
     *
     * @param generated Gemini가 생성한 구조화 답변
     * @return 정규화된 답변 문구
     */
    private String normalizeAnswer(CookingAiAnswerGeminiDto generated) {
        log.debug(
                "[CookingAiGeminiService] 요리 답변 정규화 시작 | normalizeAnswer() - START"
        );
        if (generated == null || generated.cookingRelated() == null) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }
        if (!generated.cookingRelated()) {
            log.debug(
                    "[CookingAiGeminiService] 요리 답변 정규화 종료 | normalizeAnswer() - END | cookingRelated: false"
            );
            return IRRELEVANT_ANSWER;
        }
        if (generated.answer() == null || generated.answer().isBlank()) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }
        String result = generated.answer().trim();
        log.debug(
                "[CookingAiGeminiService] 요리 답변 정규화 종료 | normalizeAnswer() - END | cookingRelated: true"
        );
        return result;
    }

    /**
     * Gemini Interactions API 요청을 전송하고 공통 예외로 변환합니다.
     *
     * @param request Gemini 상호작용 요청
     * @return Gemini 응답 JSON
     */
    private JsonNode postInteraction(CookingAiInteractionReqDto request) {
        log.debug(
                "[CookingAiGeminiService] Gemini 상호작용 요청 시작 | postInteraction() - START | model: {}",
                request.model()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(API_KEY_HEADER, properties.apiKey());
        try {
            JsonNode response = restTemplate.exchange(
                    URI.create(properties.baseUrl()),
                    HttpMethod.POST,
                    new HttpEntity<>(request, headers),
                    JsonNode.class
            ).getBody();
            if (Objects.isNull(response)) {
                throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
            }
            log.debug(
                    "[CookingAiGeminiService] Gemini 상호작용 요청 종료 | postInteraction() - END | model: {}",
                    request.model()
            );
            return response;
        } catch (CustomException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new CustomException(GeminiErrorCode.RATE_LIMIT_EXCEEDED);
            }
            throw new CustomException(GeminiErrorCode.API_CALL_FAILED);
        } catch (ResourceAccessException exception) {
            if (hasTimeoutCause(exception)) {
                throw new CustomException(GeminiErrorCode.API_TIMEOUT);
            }
            throw new CustomException(GeminiErrorCode.API_CALL_FAILED);
        } catch (RestClientException exception) {
            throw new CustomException(GeminiErrorCode.API_CALL_FAILED);
        }
    }

    /**
     * 예외 원인 체인에 소켓 타임아웃이 포함되어 있는지 확인합니다.
     *
     * @param throwable 확인할 예외
     * @return 소켓 타임아웃 포함 여부
     */
    private boolean hasTimeoutCause(Throwable throwable) {
        log.debug(
                "[CookingAiGeminiService] 타임아웃 원인 확인 시작 | hasTimeoutCause() - START"
        );
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                log.debug(
                        "[CookingAiGeminiService] 타임아웃 원인 확인 종료 | hasTimeoutCause() - END | timeout: true"
                );
                return true;
            }
            current = current.getCause();
        }
        log.debug(
                "[CookingAiGeminiService] 타임아웃 원인 확인 종료 | hasTimeoutCause() - END | timeout: false"
        );
        return false;
    }
}
