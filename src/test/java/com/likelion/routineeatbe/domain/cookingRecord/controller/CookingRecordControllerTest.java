package com.likelion.routineeatbe.domain.cookingRecord.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepTitleResDto;
import com.likelion.routineeatbe.domain.cookingRecord.service.CookingRecordService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
}
