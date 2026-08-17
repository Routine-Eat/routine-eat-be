package com.likelion.routineeatbe.domain.cookingRecord.service.gemini;

import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingSpeechGeminiReqDto;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Base64;
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
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
public class CookingSpeechGenerateGeminiService {

    private static final String API_KEY_HEADER = "x-goog-api-key";
    private static final String DEFAULT_VOICE = "Kore";
    private static final int SAMPLE_RATE = 24_000;
    private static final short CHANNELS = 1;
    private static final short BITS_PER_SAMPLE = 16;
    private static final int MAX_PCM_BYTES = 5 * 1024 * 1024;

    private final RestTemplate restTemplate;
    private final GeminiProperties properties;
    private final PcmWaveEncoder pcmWaveEncoder;

    public CookingSpeechGenerateGeminiService(
            @Qualifier("geminiRestTemplate") RestTemplate restTemplate,
            GeminiProperties properties,
            PcmWaveEncoder pcmWaveEncoder
    ) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.pcmWaveEncoder = pcmWaveEncoder;
    }

    /**
     * (1) 작업 목적
     * Gemini가 생성한 요리 답변 텍스트를 클라이언트 재생용 WAV 음성으로 변환합니다.
     *
     * (2) 세부 작업 내용
     * - 설정된 요리 음성 모델에 단일 화자 TTS 요청을 보냅니다.
     * - Base64로 반환된 PCM 데이터를 검증하고 디코딩합니다.
     * - raw PCM에 24kHz mono 16-bit WAV 헤더를 추가합니다.
     *
     * @param answer 음성으로 변환할 AI 답변 텍스트
     * @return WAV 형식 음성 바이너리
     */
    public byte[] generate(String answer) {
        log.info(
                "[CookingSpeechGenerateGeminiService] 요리 답변 음성 생성 시작 | generate() - START | answerLength: {}",
                answer.length()
        );

        CookingSpeechGeminiReqDto request = CookingSpeechGeminiReqDto.create(
                properties.cookingTranslateModel(),
                "다음 문장을 자연스럽고 차분한 한국어로 그대로 읽어주세요: " + answer,
                DEFAULT_VOICE
        );
        JsonNode response = postInteraction(request);
        byte[] pcmData = decodePcmData(response);
        byte[] result = pcmWaveEncoder.encode(
                pcmData,
                SAMPLE_RATE,
                CHANNELS,
                BITS_PER_SAMPLE
        );

        log.info(
                "[CookingSpeechGenerateGeminiService] 요리 답변 음성 생성 종료 | generate() - END | audioSize: {}",
                result.length
        );
        return result;
    }

    /**
     * Gemini TTS 응답에서 Base64 PCM 데이터를 찾아 디코딩합니다.
     *
     * @param response Gemini TTS 응답 JSON
     * @return 디코딩된 PCM 바이너리
     */
    private byte[] decodePcmData(JsonNode response) {
        log.debug(
                "[CookingSpeechGenerateGeminiService] PCM 데이터 디코딩 시작 | decodePcmData() - START"
        );
        String encodedAudio = response.at("/output_audio/data").asText();
        if (encodedAudio.isBlank()) {
            encodedAudio = findAudioInSteps(response.path("steps"));
        }
        if (encodedAudio.isBlank()) {
            throw new CustomException(GeminiErrorCode.INVALID_AUDIO_RESPONSE);
        }
        try {
            byte[] result = Base64.getDecoder().decode(encodedAudio);
            if (result.length == 0 || result.length > MAX_PCM_BYTES) {
                throw new CustomException(GeminiErrorCode.INVALID_AUDIO_RESPONSE);
            }
            log.debug(
                    "[CookingSpeechGenerateGeminiService] PCM 데이터 디코딩 종료 | decodePcmData() - END | pcmSize: {}",
                    result.length
            );
            return result;
        } catch (IllegalArgumentException exception) {
            throw new CustomException(GeminiErrorCode.INVALID_AUDIO_RESPONSE);
        }
    }

    /**
     * Gemini 단계 목록에서 첫 번째 음성 데이터를 조회합니다.
     *
     * @param steps Gemini 응답 단계 목록
     * @return Base64 음성 데이터, 없으면 빈 문자열
     */
    private String findAudioInSteps(JsonNode steps) {
        log.debug(
                "[CookingSpeechGenerateGeminiService] 단계 음성 탐색 시작 | findAudioInSteps() - START"
        );
        if (!steps.isArray()) {
            log.debug(
                    "[CookingSpeechGenerateGeminiService] 단계 음성 탐색 종료 | findAudioInSteps() - END | found: false"
            );
            return "";
        }
        for (JsonNode step : steps) {
            JsonNode content = step.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode item : content) {
                if ("audio".equals(item.path("type").asText())
                        && !item.path("data").asText().isBlank()) {
                    log.debug(
                            "[CookingSpeechGenerateGeminiService] 단계 음성 탐색 종료 | findAudioInSteps() - END | found: true"
                    );
                    return item.path("data").asText();
                }
            }
        }
        log.debug(
                "[CookingSpeechGenerateGeminiService] 단계 음성 탐색 종료 | findAudioInSteps() - END | found: false"
        );
        return "";
    }

    /**
     * Gemini Interactions API에 TTS 요청을 전송합니다.
     *
     * @param request Gemini TTS 요청
     * @return Gemini 응답 JSON
     */
    private JsonNode postInteraction(CookingSpeechGeminiReqDto request) {
        log.debug(
                "[CookingSpeechGenerateGeminiService] Gemini TTS 요청 시작 | postInteraction() - START | model: {}",
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
            if (response == null) {
                throw new CustomException(GeminiErrorCode.INVALID_AUDIO_RESPONSE);
            }
            log.debug(
                    "[CookingSpeechGenerateGeminiService] Gemini TTS 요청 종료 | postInteraction() - END | model: {}",
                    request.model()
            );
            return response;
        } catch (CustomException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new CustomException(GeminiErrorCode.RATE_LIMIT_EXCEEDED);
            }
            throw new CustomException(GeminiErrorCode.AUDIO_GENERATION_FAILED);
        } catch (ResourceAccessException exception) {
            if (hasTimeoutCause(exception)) {
                throw new CustomException(GeminiErrorCode.API_TIMEOUT);
            }
            throw new CustomException(GeminiErrorCode.AUDIO_GENERATION_FAILED);
        } catch (RestClientException exception) {
            throw new CustomException(GeminiErrorCode.AUDIO_GENERATION_FAILED);
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
                "[CookingSpeechGenerateGeminiService] 타임아웃 원인 확인 시작 | hasTimeoutCause() - START"
        );
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                log.debug(
                        "[CookingSpeechGenerateGeminiService] 타임아웃 원인 확인 종료 | hasTimeoutCause() - END | timeout: true"
                );
                return true;
            }
            current = current.getCause();
        }
        log.debug(
                "[CookingSpeechGenerateGeminiService] 타임아웃 원인 확인 종료 | hasTimeoutCause() - END | timeout: false"
        );
        return false;
    }
}
