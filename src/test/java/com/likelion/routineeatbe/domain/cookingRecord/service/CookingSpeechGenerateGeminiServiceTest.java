package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingSpeechGenerateGeminiService;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.PcmWaveEncoder;
import com.likelion.routineeatbe.global.config.GeminiProperties;
import com.likelion.routineeatbe.global.exception.CustomException;
import com.likelion.routineeatbe.global.exception.GeminiErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class CookingSpeechGenerateGeminiServiceTest {

    @Test
    @DisplayName("Gemini Base64 PCM 응답을 WAV 음성으로 변환 성공")
    void Gemini_PCM_WAV_음성_변환_성공() {
        // given
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        CookingSpeechGenerateGeminiService service = new CookingSpeechGenerateGeminiService(
                restTemplate,
                createProperties(),
                new PcmWaveEncoder()
        );
        server.expect(requestTo("https://example.com/interactions"))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andRespond(withSuccess(
                        "{\"output_audio\":{\"data\":\"AQIDBA==\"}}",
                        MediaType.APPLICATION_JSON
                ));

        // when
        byte[] result = service.generate("감자를 약한 불에서 익혀주세요.");

        // then
        assertThat(result).hasSize(48);
        assertThat(new String(result, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("RIFF");
        server.verify();
    }

    @Test
    @DisplayName("Gemini 음성 데이터가 비어 있으면 응답 검증 실패")
    void Gemini_음성_데이터_검증_실패_빈_응답() {
        // given
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        CookingSpeechGenerateGeminiService service = new CookingSpeechGenerateGeminiService(
                restTemplate,
                createProperties(),
                new PcmWaveEncoder()
        );
        server.expect(requestTo("https://example.com/interactions"))
                .andRespond(withSuccess("{\"steps\":[]}", MediaType.APPLICATION_JSON));

        // when & then
        assertThatThrownBy(() -> service.generate("답변"))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> assertThat(((CustomException) exception).getErrorCode())
                        .isEqualTo(GeminiErrorCode.INVALID_AUDIO_RESPONSE));
    }

    private GeminiProperties createProperties() {
        return new GeminiProperties(
                "https://example.com/interactions",
                "test-key",
                "gemini-3.1-flash-lite",
                "gemini-3.1-flash-tts-preview",
                "menu-model",
                "food-model",
                "equipment-model",
                "step-model",
                10,
                new GeminiProperties.Retry(
                        1,
                        Duration.ofMillis(1),
                        Duration.ofMillis(1),
                        0.0
                )
        );
    }
}
