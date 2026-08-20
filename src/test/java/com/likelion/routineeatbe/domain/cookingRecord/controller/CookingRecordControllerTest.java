package com.likelion.routineeatbe.domain.cookingRecord.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiResult;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingAiReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingResultSaveReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingRecordSearchReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingSessionLogSearchReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.ModifiedCookingRecordFoodIngredientReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingAiAnswerResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingCompleteResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientAmountResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordInProgressResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordListItemResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordStepTitlesResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingSessionLogItemResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingSessionLogListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepFoodIngredientResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTipResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTitleResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CurrentCookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CurrentCookingStepFoodIngredientResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CurrentCookingStepResDto;
import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.cookingRecord.service.CookingAiService;
import com.likelion.routineeatbe.domain.cookingRecord.service.CookingRecordService;
import com.likelion.routineeatbe.domain.cookingSession.enums.CookingSessionLogType;
import com.likelion.routineeatbe.domain.cookingTip.enums.CookingTipContentType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CookingRecordController.class)
class CookingRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CookingRecordService cookingRecordService;

    @MockitoBean
    private CookingAiService cookingAiService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("진행 중인 요리 세션 조회 API 성공")
    void 진행_중인_요리_세션_조회_API_성공() throws Exception {
        // given
        CookingRecordInProgressResDto response = CookingRecordInProgressResDto.create(1L);
        given(cookingRecordService.getInProgressCookingRecord("1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/cooking-records/current")
                        .param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value(
                        "해당 사용자가 진행 중인 요리 세션 조회에 성공했습니다."
                ))
                .andExpect(jsonPath("$.data.cookingRecordId").value(1));
        then(cookingRecordService).should().getInProgressCookingRecord("1234");
    }

    @Test
    @DisplayName("진행 중인 요리 전체 단계 조회 API 성공")
    void 진행_중인_요리_전체_단계_조회_API_성공() throws Exception {
        // given
        CookingRecordStepTitlesResDto response = CookingRecordStepTitlesResDto.create(
                2,
                List.of(
                        CookingStepTitleResDto.create(1L, "재료 준비"),
                        CookingStepTitleResDto.create(2L, "대파 볶기")
                )
        );
        given(cookingRecordService.getInProgressCookingStepTitles("1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/cooking-records/current/cooking-steps")
                        .param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("요리 전체 단계 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.cookingStepCount").value(2))
                .andExpect(jsonPath("$.data.cookingStepTitles[0].stepLevel").value(1))
                .andExpect(jsonPath("$.data.cookingStepTitles[0].stepTitle").value("재료 준비"))
                .andExpect(jsonPath("$.data.cookingStepTitles[1].stepLevel").value(2))
                .andExpect(jsonPath("$.data.cookingStepTitles[1].stepTitle").value("대파 볶기"));
        then(cookingRecordService).should().getInProgressCookingStepTitles("1234");
    }

    @Test
    @DisplayName("진행 중인 요리 전체 단계 조회 API 실패 - 잘못된 사용자 고유 식별번호")
    void 진행_중인_요리_전체_단계_조회_API_실패_잘못된_사용자_고유_식별번호() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/cooking-records/current/cooking-steps")
                        .param("userNumber", "12AB"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("진행 중인 요리 세션 조회 API 실패 - 잘못된 사용자 고유 식별번호")
    void 진행_중인_요리_세션_조회_API_실패_잘못된_사용자_고유_식별번호() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/cooking-records/current")
                        .param("userNumber", "12"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 중 AI 질문 API 성공 - multipart 응답 반환")
    void 요리_중_AI_질문_API_성공() throws Exception {
        // given
        CookingAiReqDto request = new CookingAiReqDto("조린다는 게 뭐야?");
        CookingAiResult response = CookingAiResult.create(
                "응답이 반환되었습니다.",
                CookingAiAnswerResDto.create("약한 불에서 국물이 배도록 익히는 뜻이에요."),
                new byte[]{1, 2, 3}
        );
        given(cookingAiService.interact(10L, "1234", request)).willReturn(response);

        // when & then
        mockMvc.perform(post(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/ai",
                        10L
                )
                        .param("userNumber", "1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(result -> {
                    String multipartBody = result.getResponse()
                            .getContentAsString(StandardCharsets.UTF_8);
                    assertThat(multipartBody)
                            .contains("name=\"response\"")
                            .contains("Content-Type: application/json")
                            .contains("약한 불에서 국물이 배도록 익히는 뜻이에요.")
                            .contains("name=\"audio\"; filename=\"cooking-ai-answer.wav\"")
                            .contains("Content-Type: audio/wav");
                });
        then(cookingAiService).should().interact(10L, "1234", request);
    }

    @Test
    @DisplayName("요리 중 AI 질문 API 실패 - 빈 사용자 발화")
    void 요리_중_AI_질문_API_실패_빈_사용자_발화() throws Exception {
        // given
        CookingAiReqDto request = new CookingAiReqDto(" ");

        // when & then
        mockMvc.perform(post(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/ai",
                        10L
                )
                        .param("userNumber", "1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        then(cookingAiService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 기록 목록 조회 API 성공 - 기본 커서와 크기 적용")
    void 요리_기록_목록_조회_API_성공() throws Exception {
        // given
        CookingRecordListResDto response = CookingRecordListResDto.create(
                List.of(CookingRecordListItemResDto.create(
                        659L,
                        "감자미역국",
                        "https://example.com/menu.jpg",
                        true,
                        LocalDate.of(2026, 8, 15),
                        DifficultyLevel.LEVEL_1,
                        8L
                )),
                true,
                11
        );
        given(cookingRecordService.getCookingRecords(
                new CookingRecordSearchReqDto("1234", 1, 10)
        )).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/cooking-records")
                        .param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message")
                        .value("요리 기록(회고록) 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].recipeId").value(659))
                .andExpect(jsonPath("$.data.content[0].menuName").value("감자미역국"))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl")
                        .value("https://example.com/menu.jpg"))
                .andExpect(jsonPath("$.data.content[0].isFavoriteRecipe").value(true))
                .andExpect(jsonPath("$.data.content[0].completedAt").value("2026-08-15"))
                .andExpect(jsonPath("$.data.content[0].userDifficultyLevel")
                        .value("LEVEL_1"))
                .andExpect(jsonPath("$.data.content[0].usedFoodIngredientCount").value(8))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(11));
        then(cookingRecordService).should().getCookingRecords(
                new CookingRecordSearchReqDto("1234", 1, 10)
        );
    }

    @Test
    @DisplayName("AI 대화 기록 조회 API 성공 - 기본 커서와 크기 적용")
    void AI_대화_기록_조회_API_성공() throws Exception {
        // given
        CookingSessionLogListResDto response = CookingSessionLogListResDto.create(
                List.of(
                        CookingSessionLogItemResDto.create(
                                10L,
                                CookingSessionLogType.USER,
                                "굴소스가 한 스푼밖에 없는데 어떡해?"
                        ),
                        CookingSessionLogItemResDto.create(
                                11L,
                                CookingSessionLogType.AI,
                                "간장을 반 스푼 추가해보세요."
                        )
                ),
                true,
                11
        );
        given(cookingRecordService.getCookingSessionLogs(
                10L,
                new CookingSessionLogSearchReqDto("1234", 1, 10)
        )).willReturn(response);

        // when & then
        mockMvc.perform(get(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/ai",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message")
                        .value("AI 대화 기록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].cookingSessionLogId").value(10))
                .andExpect(jsonPath("$.data.content[0].cookingSessionLogType")
                        .value("USER"))
                .andExpect(jsonPath("$.data.content[0].cookingSessionLogContent")
                        .value("굴소스가 한 스푼밖에 없는데 어떡해?"))
                .andExpect(jsonPath("$.data.content[1].cookingSessionLogType")
                        .value("AI"))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(11));
        then(cookingRecordService).should().getCookingSessionLogs(
                10L,
                new CookingSessionLogSearchReqDto("1234", 1, 10)
        );
    }

    @Test
    @DisplayName("AI 대화 기록 조회 API 실패 - 커서가 1 미만")
    void AI_대화_기록_조회_API_실패_잘못된_커서() throws Exception {
        // when & then
        mockMvc.perform(get(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/ai",
                        10L
                )
                        .param("userNumber", "1234")
                        .param("cursor", "0"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 기록 목록 조회 API 실패 - 커서가 1 미만")
    void 요리_기록_목록_조회_API_실패_잘못된_커서() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/cooking-records")
                        .param("userNumber", "1234")
                        .param("cursor", "0"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 기록 상세 조회 API 성공 - 200 반환")
    void 요리_기록_상세_조회_API_성공() throws Exception {
        // given
        CookingRecordDetailResDto response = CookingRecordDetailResDto.create(
                10L,
                "감자미역국",
                "https://example.com/menu.jpg",
                20,
                DifficultyLevel.LEVEL_2,
                TasteRating.LEVEL_1,
                DifficultyLevel.LEVEL_3,
                "참기름을 조금 더 넣으면 맛있습니다.",
                "https://api-img.nahjjun.cloud/1/10/result.jpg"
        );
        given(cookingRecordService.getCookingRecordDetail(10L, "1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/cooking-records/{cookingRecordId}", 10L)
                        .param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message")
                        .value("요리 기록(회고록) 상세 조회에 성공하였습니다."))
                .andExpect(jsonPath("$.data.cookingRecordId").value(10))
                .andExpect(jsonPath("$.data.menuName").value("감자미역국"))
                .andExpect(jsonPath("$.data.thumbnailUrl")
                        .value("https://example.com/menu.jpg"))
                .andExpect(jsonPath("$.data.timeRequired").value(20))
                .andExpect(jsonPath("$.data.difficultyLevel").value("LEVEL_2"))
                .andExpect(jsonPath("$.data.userTasteRating").value("LEVEL_1"))
                .andExpect(jsonPath("$.data.userDifficultyLevel").value("LEVEL_3"))
                .andExpect(jsonPath("$.data.cookingTip")
                        .value("참기름을 조금 더 넣으면 맛있습니다."))
                .andExpect(jsonPath("$.data.userCookingRecordPhotoUrl")
                        .value("https://api-img.nahjjun.cloud/1/10/result.jpg"));
        then(cookingRecordService).should().getCookingRecordDetail(10L, "1234");
    }

    @Test
    @DisplayName("요리 기록 상세 조회 API 실패 - 잘못된 사용자 번호")
    void 요리_기록_상세_조회_API_실패_잘못된_사용자_번호() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/cooking-records/{cookingRecordId}", 10L)
                        .param("userNumber", "12AB"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이번 요리에 사용한 음식 재료 양 조회 API 성공 - 200 반환")
    void 이번_요리_사용_음식_재료_양_조회_API_성공() throws Exception {
        // given
        CookingRecordFoodIngredientsResDto response =
                CookingRecordFoodIngredientsResDto.create(List.of(
                        CookingRecordFoodIngredientAmountResDto.create(
                                1L,
                                2L,
                                "계란",
                                300.0,
                                140.0,
                                PrimaryUnit.G,
                                8.0,
                                2.0,
                                SecondaryUnit.AL
                        )
                ));
        given(cookingRecordService.getFoodIngredients(10L, "1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(get(
                        "/api/v1/cooking-records/{cookingRecordId}/food-ingredients",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("성공했습니다."))
                .andExpect(jsonPath(
                        "$.data.foodIngredients[0].cookingRecordFoodIngredientId"
                ).value(1))
                .andExpect(jsonPath("$.data.foodIngredients[0].foodIngredientId").value(2))
                .andExpect(jsonPath("$.data.foodIngredients[0].name").value("계란"))
                .andExpect(jsonPath(
                        "$.data.foodIngredients[0].prevPrimaryAmountValue"
                ).value(300.0))
                .andExpect(jsonPath(
                        "$.data.foodIngredients[0].currentPrimaryAmountValue"
                ).value(140.0))
                .andExpect(jsonPath("$.data.foodIngredients[0].primaryUnit").value("G"))
                .andExpect(jsonPath(
                        "$.data.foodIngredients[0].prevSecondaryAmountValue"
                ).value(8.0))
                .andExpect(jsonPath(
                        "$.data.foodIngredients[0].currentSecondaryAmountValue"
                ).value(2.0))
                .andExpect(jsonPath("$.data.foodIngredients[0].secondaryUnit")
                        .value("AL"));
        then(cookingRecordService).should().getFoodIngredients(10L, "1234");
    }

    @Test
    @DisplayName("이번 요리에 사용한 음식 재료 양 조회 API 실패 - 양수가 아닌 요리 기록 PK")
    void 이번_요리_사용_음식_재료_양_조회_API_실패_양수가_아닌_요리_기록_PK()
            throws Exception {
        // when & then
        mockMvc.perform(get(
                        "/api/v1/cooking-records/{cookingRecordId}/food-ingredients",
                        0L
                ).param("userNumber", "1234"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 결과 저장 API 성공 - 이미지 포함 201 반환")
    void 요리_결과_저장_API_이미지_포함_성공() throws Exception {
        // given
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                TasteRating.LEVEL_3,
                DifficultyLevel.LEVEL_2,
                List.of(new ModifiedCookingRecordFoodIngredientReqDto(
                        1L,
                        80.0,
                        null
                ))
        );
        CookingResultSaveResDto response = CookingResultSaveResDto.create(10L);
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "result.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image-data".getBytes()
        );
        given(cookingRecordService.saveCookingResult("1234", request, image))
                .willReturn(response);

        // when & then
        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/v1/cooking-records")
                        .file(requestPart)
                        .file(image)
                        .param("userNumber", "1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("성공했습니다."))
                .andExpect(jsonPath("$.data.savedCookingRecordId").value(10));
        then(cookingRecordService).should().saveCookingResult("1234", request, image);
    }

    @Test
    @DisplayName("요리 결과 저장 API 성공 - 이미지 생략")
    void 요리_결과_저장_API_이미지_생략_성공() throws Exception {
        // given
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                TasteRating.LEVEL_2,
                DifficultyLevel.LEVEL_1,
                List.of()
        );
        CookingResultSaveResDto response = CookingResultSaveResDto.create(10L);
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
        given(cookingRecordService.saveCookingResult("1234", request, null))
                .willReturn(response);

        // when & then
        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/v1/cooking-records")
                        .file(requestPart)
                        .param("userNumber", "1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.savedCookingRecordId").value(10));
        then(cookingRecordService).should().saveCookingResult("1234", request, null);
    }

    @Test
    @DisplayName("요리 결과 저장 API 실패 - 맛 평가 누락")
    void 요리_결과_저장_API_실패_맛_평가_누락() throws Exception {
        // given
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                null,
                DifficultyLevel.LEVEL_1,
                List.of()
        );
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        // when & then
        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/v1/cooking-records")
                        .file(requestPart)
                        .param("userNumber", "1234"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 결과 저장 API 실패 - 음식 재료 사용량 음수")
    void 요리_결과_저장_API_실패_음식_재료_사용량_음수() throws Exception {
        // given
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                TasteRating.LEVEL_2,
                DifficultyLevel.LEVEL_1,
                List.of(new ModifiedCookingRecordFoodIngredientReqDto(
                        1L,
                        -1.0,
                        null
                ))
        );
        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        // when & then
        mockMvc.perform(multipart(HttpMethod.PATCH, "/api/v1/cooking-records")
                        .file(requestPart)
                        .param("userNumber", "1234"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 시작 API 성공 - 201 반환")
    void 요리_시작_API_성공_201_반환() throws Exception {
        // given
        CookingStartReqDto request = new CookingStartReqDto(1L, 2);
        CookingStartResDto response = CookingStartResDto.create(
                10L,
                "계란 대파 볶음밥",
                "https://example.com/thumbnail.jpg",
                8,
                List.of("프라이팬의 물기를 확인하세요."),
                1,
                0,
                null,
                CookingStepDetailResDto.create(
                        20L,
                        1L,
                        "재료 준비",
                        null,
                        "대파를 잘라주세요.",
                        null,
                        List.of(CookingStepTipResDto.create(
                                5L,
                                1,
                                "대파 써는 법",
                                "대파를 세로로 고정해 주세요.",
                                CookingTipContentType.TEXT
                        )),
                        createNavigationFoodIngredients()
                ),
                List.of(CookingStepTitleResDto.create(1L, "재료 준비"))
        );
        given(cookingRecordService.startCooking("1234", request)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/cooking-records")
                        .param("userNumber", "1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("요리 시작에 성공했습니다."))
                .andExpect(jsonPath("$.data.cookingRecordId").value(10))
                .andExpect(jsonPath("$.data.checkListBeforeStart[0]")
                        .value("프라이팬의 물기를 확인하세요."))
                .andExpect(jsonPath("$.data.cookingStepCount").value(1))
                .andExpect(jsonPath("$.data.prevCookingStepLevel").value(0))
                .andExpect(jsonPath("$.data.nextCookingStepLevel").doesNotExist())
                .andExpect(jsonPath("$.data.currentCookingStep.cookingStepId").value(20))
                .andExpect(jsonPath("$.data.currentCookingStep.level").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipId").value(5))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].sortNum").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipTitle")
                        .value("대파 써는 법"))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipContent")
                        .value("대파를 세로로 고정해 주세요."))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipType")
                        .value("TEXT"))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].cookingRecordFoodIngredientId")
                        .value(30))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].foodIngredientId")
                        .value(7))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].name")
                        .value("대파"))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].primaryUsedAmountValue")
                        .value(60.0))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].primaryUnit")
                        .value("G"))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].secondaryUsedAmountValue")
                        .value(0.5))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].secondaryUnit")
                        .value("JULGI"))
                .andExpect(jsonPath("$.data.currentCookingStep.stepTips").doesNotExist())
                .andExpect(jsonPath("$.data.cookingStepTitles[0].stepLevel").value(1));
        then(cookingRecordService).should().startCooking("1234", request);
    }

    @Test
    @DisplayName("요리 시작 API 실패 - 잘못된 사용자 번호")
    void 요리_시작_API_실패_잘못된_사용자_번호() throws Exception {
        // given
        CookingStartReqDto request = new CookingStartReqDto(1L, 2);

        // when & then
        mockMvc.perform(post("/api/v1/cooking-records")
                        .param("userNumber", "12AB")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("요리 시작 API 실패 - 인분 수가 1 미만")
    void 요리_시작_API_실패_인분_수_1_미만() throws Exception {
        // given
        CookingStartReqDto request = new CookingStartReqDto(1L, 0);

        // when & then
        mockMvc.perform(post("/api/v1/cooking-records")
                        .param("userNumber", "1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("현재 요리 단계 조회 API 성공 - 200 반환")
    void 현재_요리_단계_조회_API_성공_200_반환() throws Exception {
        // given
        CurrentCookingStepResDto response = CurrentCookingStepResDto.create(
                10,
                0,
                2,
                CurrentCookingStepDetailResDto.create(
                        1L,
                        "재료 준비: 대파 준비하기",
                        "https://example.com/step.jpg",
                        "대파를 잘라주세요.",
                        "가위를 사용해도 괜찮아요.",
                        createNavigationTips(),
                        List.of(CurrentCookingStepFoodIngredientResDto.create(
                                30L,
                                7L,
                                "대파",
                                60.0,
                                PrimaryUnit.G,
                                0.5,
                                SecondaryUnit.JULGI
                        ))
                )
        );
        given(cookingRecordService.getCurrentCookingStep(10L, "1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(get(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/current",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("현재 1번째 단계입니다."))
                .andExpect(jsonPath("$.data.cookingStepCount").value(10))
                .andExpect(jsonPath("$.data.prevCookingStepLevel").value(0))
                .andExpect(jsonPath("$.data.nextCookingStepLevel").value(2))
                .andExpect(jsonPath("$.data.currentCookingStep.level").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.cookingStepId")
                        .doesNotExist())
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipType")
                        .value("TEXT"))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].primaryAmountValue"
                ).value(60.0))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].secondaryAmountValue"
                ).value(0.5))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].primaryUsedAmountValue"
                ).doesNotExist());
        then(cookingRecordService).should().getCurrentCookingStep(10L, "1234");
    }

    @Test
    @DisplayName("마지막 요리 단계 변경 API 성공 - 201 반환")
    void 마지막_요리_단계_변경_API_성공_201_반환() throws Exception {
        // given
        CurrentCookingStepResDto response = CurrentCookingStepResDto.create(
                10,
                9,
                null,
                CurrentCookingStepDetailResDto.create(
                        10L,
                        "요리 완성",
                        "https://example.com/last-step.jpg",
                        "불을 끄고 요리를 완성하세요.",
                        "그릇에 조심히 담아주세요.",
                        createNavigationTips(),
                        List.of(CurrentCookingStepFoodIngredientResDto.create(
                                30L,
                                7L,
                                "대파",
                                60.0,
                                PrimaryUnit.G,
                                0.5,
                                SecondaryUnit.JULGI
                        ))
                )
        );
        given(cookingRecordService.moveToLastCookingStep(10L, "1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/last",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value(
                        "마지막 요리 단계로 이동했습니다. 현재 10번째 단계입니다."
                ))
                .andExpect(jsonPath("$.data.cookingStepCount").value(10))
                .andExpect(jsonPath("$.data.prevCookingStepLevel").value(9))
                .andExpect(jsonPath("$.data.nextCookingStepLevel").doesNotExist())
                .andExpect(jsonPath("$.data.currentCookingStep.level").value(10))
                .andExpect(jsonPath("$.data.currentCookingStep.cookingStepId")
                        .doesNotExist())
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipType")
                        .value("TEXT"))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].primaryAmountValue"
                ).value(60.0));
        then(cookingRecordService).should().moveToLastCookingStep(10L, "1234");
    }

    @Test
    @DisplayName("마지막 요리 단계 변경 API 실패 - 잘못된 사용자 번호")
    void 마지막_요리_단계_변경_API_실패_잘못된_사용자_번호() throws Exception {
        // when & then
        mockMvc.perform(patch(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/last",
                        10L
                ).param("userNumber", "12AB"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("다음 요리 단계 이동 API 성공 - 201 반환")
    void 다음_요리_단계_이동_API_성공_201_반환() throws Exception {
        // given
        CookingStepNavigationResDto response = CookingStepNavigationResDto.create(
                10,
                1,
                3,
                CookingStepDetailResDto.create(
                        20L,
                        2L,
                        "대파 준비하기",
                        "https://example.com/step.jpg",
                        "대파를 잘라주세요.",
                        "가위를 사용해도 괜찮아요.",
                        createNavigationTips(),
                        createNavigationFoodIngredients()
                )
        );
        given(cookingRecordService.moveToNextCookingStep(10L, "1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(post(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/next",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message")
                        .value("다음 요리 단계로 이동했습니다. 현재 2번째 단계입니다."))
                .andExpect(jsonPath("$.data.cookingStepCount").value(10))
                .andExpect(jsonPath("$.data.prevCookingStepLevel").value(1))
                .andExpect(jsonPath("$.data.nextCookingStepLevel").value(3))
                .andExpect(jsonPath("$.data.currentCookingStep.cookingStepId").value(20))
                .andExpect(jsonPath("$.data.currentCookingStep.level").value(2))
                .andExpect(jsonPath("$.data.currentCookingStep.title").value("대파 준비하기"))
                .andExpect(jsonPath("$.data.currentCookingStep.thumbnailUrl")
                        .value("https://example.com/step.jpg"))
                .andExpect(jsonPath("$.data.currentCookingStep.content")
                        .value("대파를 잘라주세요."))
                .andExpect(jsonPath("$.data.currentCookingStep.subContent")
                        .value("가위를 사용해도 괜찮아요."))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipId").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].sortNum").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipTitle")
                        .value("칼로 써는 방법 배워볼래요."))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipContent")
                        .value("칼로 썰 때는 엄지를 안쪽으로 접어주세요."))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipType")
                        .value("TEXT"))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[1].cookingTipId").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[1].sortNum").value(2))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[1].cookingTipContent")
                        .value("https://api-img.nahjjun.cloud/tip/1/2"))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[1].cookingTipType")
                        .value("IMAGE"))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].name")
                        .value("대파"))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].cookingRecordFoodIngredientId"
                ).value(30))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].foodIngredientId"
                ).value(7))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].primaryUsedAmountValue"
                ).value(60.0))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].primaryUnit"
                ).value("G"))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].secondaryUsedAmountValue"
                ).value(0.5))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].secondaryUnit"
                ).value("JULGI"))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].prevPrimaryAmountValue"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].currentPrimaryAmountValue"
                ).doesNotExist());
        then(cookingRecordService).should().moveToNextCookingStep(10L, "1234");
    }

    @Test
    @DisplayName("다음 요리 단계 이동 API 성공 - 요리 완료")
    void 다음_요리_단계_이동_API_성공_요리_완료() throws Exception {
        // given
        CookingCompleteResDto response = CookingCompleteResDto.create(
                "오징어볶음",
                LocalDate.of(2026, 8, 21)
        );
        given(cookingRecordService.moveToNextCookingStep(10L, "1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(post(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/next",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("요리가 종료되었습니다."))
                .andExpect(jsonPath("$.data.cookedMenuName").value("오징어볶음"))
                .andExpect(jsonPath("$.data.cookedDate").value("2026-08-21"));
    }

    @Test
    @DisplayName("다음 요리 단계 이동 API 실패 - 양수가 아닌 요리 기록 PK")
    void 다음_요리_단계_이동_API_실패_양수가_아닌_요리_기록_PK() throws Exception {
        // when & then
        mockMvc.perform(post(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/next",
                        0L
                ).param("userNumber", "1234"))
                .andExpect(status().isBadRequest());
        then(cookingRecordService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이전 요리 단계 이동 API 성공 - 201 반환")
    void 이전_요리_단계_이동_API_성공_201_반환() throws Exception {
        // given
        CookingStepNavigationResDto response = CookingStepNavigationResDto.create(
                10,
                0,
                2,
                CookingStepDetailResDto.create(
                        19L,
                        1L,
                        "대파 준비하기",
                        "https://example.com/step.jpg",
                        "대파를 잘라주세요.",
                        "가위를 사용해도 괜찮아요.",
                        createNavigationTips(),
                        createNavigationFoodIngredients()
                )
        );
        given(cookingRecordService.moveToPreviousCookingStep(10L, "1234"))
                .willReturn(response);

        // when & then
        mockMvc.perform(post(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/prev",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message")
                        .value("이전 요리 단계로 이동했습니다. 현재 1번째 단계입니다."))
                .andExpect(jsonPath("$.data.cookingStepCount").value(10))
                .andExpect(jsonPath("$.data.prevCookingStepLevel").value(0))
                .andExpect(jsonPath("$.data.nextCookingStepLevel").value(2))
                .andExpect(jsonPath("$.data.currentCookingStep.cookingStepId").value(19))
                .andExpect(jsonPath("$.data.currentCookingStep.level").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.title").value("대파 준비하기"))
                .andExpect(jsonPath("$.data.currentCookingStep.thumbnailUrl")
                        .value("https://example.com/step.jpg"))
                .andExpect(jsonPath("$.data.currentCookingStep.content")
                        .value("대파를 잘라주세요."))
                .andExpect(jsonPath("$.data.currentCookingStep.subContent")
                        .value("가위를 사용해도 괜찮아요."))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipId").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].sortNum").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipTitle")
                        .value("칼로 써는 방법 배워볼래요."))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipContent")
                        .value("칼로 썰 때는 엄지를 안쪽으로 접어주세요."))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[0].cookingTipType")
                        .value("TEXT"))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[1].cookingTipId").value(1))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[1].sortNum").value(2))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[1].cookingTipContent")
                        .value("https://api-img.nahjjun.cloud/tip/1/2"))
                .andExpect(jsonPath("$.data.currentCookingStep.tips[1].cookingTipType")
                        .value("IMAGE"))
                .andExpect(jsonPath("$.data.currentCookingStep.foodIngredients[0].name")
                        .value("대파"))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].cookingRecordFoodIngredientId"
                ).value(30))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].foodIngredientId"
                ).value(7))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].primaryUsedAmountValue"
                ).value(60.0))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].primaryUnit"
                ).value("G"))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].secondaryUsedAmountValue"
                ).value(0.5))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].secondaryUnit"
                ).value("JULGI"))
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].prevPrimaryAmountValue"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$.data.currentCookingStep.foodIngredients[0].currentPrimaryAmountValue"
                ).doesNotExist());
        then(cookingRecordService).should().moveToPreviousCookingStep(10L, "1234");
    }

    @Test
    @DisplayName("이전 요리 단계 이동 API 성공 - 첫 단계 경계")
    void 이전_요리_단계_이동_API_성공_첫_단계_경계() throws Exception {
        // given
        given(cookingRecordService.moveToPreviousCookingStep(10L, "1234"))
                .willReturn(null);

        // when & then
        mockMvc.perform(post(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/prev",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("1 이전 단계로 이동할 수 없습니다."))
                .andExpect(jsonPath("$.data").value(nullValue()));
        then(cookingRecordService).should().moveToPreviousCookingStep(10L, "1234");
    }

    private List<CookingStepTipResDto> createNavigationTips() {
        return List.of(
                CookingStepTipResDto.create(
                        1L,
                        1,
                        "칼로 써는 방법 배워볼래요.",
                        "칼로 썰 때는 엄지를 안쪽으로 접어주세요.",
                        CookingTipContentType.TEXT
                ),
                CookingStepTipResDto.create(
                        1L,
                        2,
                        "칼로 써는 방법 배워볼래요.",
                        "https://api-img.nahjjun.cloud/tip/1/2",
                        CookingTipContentType.IMAGE
                )
        );
    }

    private List<CookingStepFoodIngredientResDto> createNavigationFoodIngredients() {
        return List.of(CookingStepFoodIngredientResDto.create(
                30L,
                7L,
                "대파",
                60.0,
                PrimaryUnit.G,
                0.5,
                SecondaryUnit.JULGI
        ));
    }
}
