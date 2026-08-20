package com.likelion.routineeatbe.domain.cookingTip.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.routineeatbe.domain.cookingTip.dto.response.CookingTipInitResDto;
import com.likelion.routineeatbe.domain.cookingTip.service.CookingTipInitializationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CookingTipController.class)
class CookingTipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CookingTipInitializationService cookingTipInitializationService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("요리 팁 초기화 API 성공 - 201과 초기화 결과 반환")
    void 요리_팁_초기화_API_성공_201_반환() throws Exception {
        // given
        CookingTipInitResDto result = CookingTipInitResDto.create(68L, 169L);
        given(cookingTipInitializationService.initialize()).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/v1/cooking-tips/init"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("요리 팁 데이터 초기화를 성공했습니다."))
                .andExpect(jsonPath("$.data.cookingTipCount").value(68))
                .andExpect(jsonPath("$.data.cookingTipContentCount").value(169));
        then(cookingTipInitializationService).should().initialize();
    }
}
