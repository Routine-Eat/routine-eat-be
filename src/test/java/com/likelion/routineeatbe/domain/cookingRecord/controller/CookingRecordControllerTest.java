package com.likelion.routineeatbe.domain.cookingRecord.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingResultSaveReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTitleResDto;
import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.cookingRecord.service.CookingRecordService;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import java.util.List;
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
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("요리 결과 저장 API 성공 - 이미지 포함 201 반환")
    void 요리_결과_저장_API_이미지_포함_성공() throws Exception {
        // given
        CookingResultSaveReqDto request = new CookingResultSaveReqDto(
                TasteRating.LEVEL_3,
                DifficultyLevel.LEVEL_2
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
                DifficultyLevel.LEVEL_1
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
                DifficultyLevel.LEVEL_1
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
                        List.of()
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
                .andExpect(jsonPath("$.data.currentCookingStep.stepTips").isEmpty())
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
                        List.of()
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
                .andExpect(jsonPath("$.data.currentCookingStep.stepTips").isEmpty());
        then(cookingRecordService).should().moveToNextCookingStep(10L, "1234");
    }

    @Test
    @DisplayName("다음 요리 단계 이동 API 성공 - 요리 완료")
    void 다음_요리_단계_이동_API_성공_요리_완료() throws Exception {
        // given
        given(cookingRecordService.moveToNextCookingStep(10L, "1234"))
                .willReturn(null);

        // when & then
        mockMvc.perform(post(
                        "/api/v1/cooking-records/{cookingRecordId}/cooking-session/cooking-steps/next",
                        10L
                ).param("userNumber", "1234"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("요리가 종료되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
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
                        List.of()
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
                .andExpect(jsonPath("$.data.currentCookingStep.stepTips").isEmpty());
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
                .andExpect(jsonPath("$.data").doesNotExist());
        then(cookingRecordService).should().moveToPreviousCookingStep(10L, "1234");
    }
}
