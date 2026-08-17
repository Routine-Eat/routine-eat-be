package com.likelion.routineeatbe.domain.cookingRecord.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiContextDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiResult;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingAiGeminiCallDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.gemini.CookingAiGeminiFunctionDeclarationDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingAiReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingAiAnswerResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingAiGeminiService;
import com.likelion.routineeatbe.domain.cookingRecord.service.gemini.CookingSpeechGenerateGeminiService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class CookingAiServiceTest {

    @InjectMocks
    private CookingAiService cookingAiService;

    @Mock
    private CookingAiContextService cookingAiContextService;

    @Mock
    private CookingRecordService cookingRecordService;

    @Mock
    private CookingAiGeminiService cookingAiGeminiService;

    @Mock
    private CookingSpeechGenerateGeminiService cookingSpeechGenerateGeminiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("일반 요리 질문에 텍스트와 음성 응답 생성 성공")
    void 일반_요리_질문_텍스트_음성_응답_성공() {
        // given
        CookingAiReqDto request = new CookingAiReqDto("조린다는 게 뭐야?");
        CookingAiContextDto context = createContext();
        CookingAiGeminiCallDto call = CookingAiGeminiCallDto.create(
                "prompt",
                objectMapper.createObjectNode().putArray("steps"),
                "call-1",
                CookingAiGeminiFunctionDeclarationDto.REQUEST_CONTEXT_FUNCTION_NAME,
                objectMapper.createObjectNode()
        );
        byte[] audio = new byte[]{1, 2, 3};
        given(cookingAiContextService.prepareContext(10L, "1234", request.userSpeechText()))
                .willReturn(context);
        given(cookingAiGeminiService.startInteraction(context, request.userSpeechText()))
                .willReturn(call);
        given(cookingAiGeminiService.generateAnswer(call, context, request.userSpeechText()))
                .willReturn("약한 불에서 국물이 배도록 익히는 뜻이에요.");
        given(cookingSpeechGenerateGeminiService.generate(
                "약한 불에서 국물이 배도록 익히는 뜻이에요."
        )).willReturn(audio);

        // when
        CookingAiResult result = cookingAiService.interact(10L, "1234", request);

        // then
        assertThat(result.message()).isEqualTo("응답이 반환되었습니다.");
        assertThat(((CookingAiAnswerResDto) result.data()).answer())
                .isEqualTo("약한 불에서 국물이 배도록 익히는 뜻이에요.");
        assertThat(result.audio()).containsExactly(audio);
        then(cookingAiContextService).should().saveAiLog(
                100L,
                "약한 불에서 국물이 배도록 익히는 뜻이에요."
        );
    }

    @Test
    @DisplayName("다음 단계 이동 명령은 음성을 생성하지 않고 시스템 로그 저장 성공")
    void 다음_단계_이동_명령_시스템_로그_저장_성공() {
        // given
        CookingAiReqDto request = new CookingAiReqDto("다 했어");
        CookingAiContextDto context = createContext();
        CookingAiGeminiCallDto call = CookingAiGeminiCallDto.create(
                "prompt",
                objectMapper.createObjectNode().putArray("steps"),
                "call-2",
                CookingAiGeminiFunctionDeclarationDto.MOVE_NEXT_FUNCTION_NAME,
                objectMapper.createObjectNode()
        );
        CookingStepNavigationResDto navigation = CookingStepNavigationResDto.create(
                3,
                1,
                3,
                CookingStepDetailResDto.create(
                        2L,
                        2L,
                        "볶기",
                        null,
                        "재료를 볶아주세요.",
                        null,
                        List.of(),
                        List.of()
                )
        );
        given(cookingAiContextService.prepareContext(10L, "1234", request.userSpeechText()))
                .willReturn(context);
        given(cookingAiGeminiService.startInteraction(context, request.userSpeechText()))
                .willReturn(call);
        given(cookingRecordService.moveToNextCookingStep(10L, "1234"))
                .willReturn(navigation);

        // when
        CookingAiResult result = cookingAiService.interact(10L, "1234", request);

        // then
        assertThat(result.data()).isSameAs(navigation);
        assertThat(result.audio()).isNull();
        then(cookingAiContextService).should().saveSystemLog(
                100L,
                "다음 요리 단계로 이동했습니다. 현재 2번째 단계입니다."
        );
        then(cookingSpeechGenerateGeminiService).shouldHaveNoInteractions();
    }

    private CookingAiContextDto createContext() {
        return new CookingAiContextDto(
                1L,
                10L,
                100L,
                20L,
                1,
                1,
                3,
                "감자조림",
                "SIDE_DISH",
                "LEVEL_1",
                20,
                150.0,
                List.of(new CookingAiContextDto.CookingStepContext(
                        1L,
                        "감자 익히기",
                        "감자를 익혀주세요.",
                        null
                )),
                List.of(new CookingAiContextDto.FoodIngredientContext(
                        "감자",
                        200.0,
                        "그램",
                        2.0,
                        "개"
                ))
        );
    }
}
