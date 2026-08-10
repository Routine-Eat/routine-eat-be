package com.likelion.routineeatbe.global.util;

import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.domain.menu.dto.gemini.MenuAndRecipeGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.global.dto.gemini.GeminiInteractionReqDto;
import com.likelion.routineeatbe.global.dto.gemini.GeminiInteractionResDto;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiUtil {

    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final String FUNCTION_CALL_TYPE = "function_call";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeminiProperties properties;

    public GeminiUtil(
            @Qualifier("geminiRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper,
            GeminiProperties properties
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Gemini Interactions API를 Function Calling 방식으로 호출합니다.
     *
     * @param prompt Gemini에 전달할 입력 프롬프트
     * @param functionDeclaration 호출을 강제할 함수 선언
     * @param responseType Function Call arguments를 변환할 타입
     * @param <T> Function Call 결과 타입
     * @return Function Call arguments를 변환한 결과
     */
    public <T> T callFunction(
            String prompt,
            MenuAndRecipeGeminiFunctionDeclarationDto functionDeclaration,
            Class<T> responseType
    ) {
        log.info(
                "[GeminiUtil] Gemini Function Call 시작 | callFunction() - START | model: {}, functionName: {}",
                properties.model(),
                functionDeclaration.name()
        );

        GeminiInteractionReqDto request = GeminiInteractionReqDto.create(
                properties.model(),
                prompt,
                functionDeclaration
        );
        HttpEntity<GeminiInteractionReqDto> httpEntity = new HttpEntity<>(request, createHeaders());

        try {
            GeminiInteractionResDto response = restTemplate.exchange(
                    URI.create(properties.baseUrl()),
                    HttpMethod.POST,
                    httpEntity,
                    GeminiInteractionResDto.class
            ).getBody();
            T result = parseFunctionCall(response, functionDeclaration.name(), responseType);

            log.info(
                    "[GeminiUtil] Gemini Function Call 종료 | callFunction() - END | functionName: {}",
                    functionDeclaration.name()
            );
            return result;
        } catch (CustomException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                log.warn(
                        "[GeminiUtil] Gemini API 호출 한도 초과 | functionName: {}, errorLog: {}",
                        functionDeclaration.name(),
                        exception.getMessage()
                );
                throw new CustomException(GeminiErrorCode.RATE_LIMIT_EXCEEDED);
            }
            log.error(
                    "[GeminiUtil] Gemini API 호출 실패 | functionName: {}, status: {}, errorLog: {}",
                    functionDeclaration.name(),
                    exception.getStatusCode().value(),
                    exception.getMessage()
            );
            throw new CustomException(GeminiErrorCode.API_CALL_FAILED);
        } catch (ResourceAccessException exception) {
            if (hasTimeoutCause(exception)) {
                log.error("[GeminiUtil] Gemini API 응답 시간 초과 | functionName: {}, errorLog: {}", functionDeclaration.name(), exception.getMessage());
                throw new CustomException(GeminiErrorCode.API_TIMEOUT);
            }
            log.error("[GeminiUtil] Gemini API 연결 실패 | functionName: {}, errorLog: {}", functionDeclaration.name(), exception.getMessage());
            throw new CustomException(GeminiErrorCode.API_CALL_FAILED);
        } catch (RestClientException exception) {
            log.error("[GeminiUtil] Gemini API 호출 실패 | functionName: {}, errorLog: {}", functionDeclaration.name(), exception.getMessage());
            throw new CustomException(GeminiErrorCode.API_CALL_FAILED);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(API_KEY_HEADER, properties.apiKey());
        return headers;
    }

    private <T> T parseFunctionCall(
            GeminiInteractionResDto response,
            String functionName,
            Class<T> responseType
    ) {
        if (Objects.isNull(response) || Objects.isNull(response.steps())) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }

        GeminiInteractionResDto.Step functionCall = response.steps().stream()
                .filter(step -> FUNCTION_CALL_TYPE.equals(step.type()))
                .filter(step -> functionName.equals(step.name()))
                .findFirst()
                .orElseThrow(() -> new CustomException(GeminiErrorCode.INVALID_FUNCTION_CALL));

        if (Objects.isNull(functionCall.arguments())) {
            throw new CustomException(GeminiErrorCode.INVALID_FUNCTION_CALL);
        }

        try {
            return objectMapper.treeToValue(functionCall.arguments(), responseType);
        } catch (JacksonException exception) {
            throw new CustomException(GeminiErrorCode.INVALID_RESPONSE);
        }
    }

    private boolean hasTimeoutCause(Throwable throwable) {
        Throwable current = throwable;
        while (Objects.nonNull(current)) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
