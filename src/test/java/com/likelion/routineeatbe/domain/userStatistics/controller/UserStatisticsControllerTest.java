package com.likelion.routineeatbe.domain.userStatistics.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsRecipeReportResDto;
import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsResDto;
import com.likelion.routineeatbe.domain.userStatistics.service.UserStatisticsService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserStatisticsController.class)
class UserStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserStatisticsService userStatisticsService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("사용자 통계 조회 API 성공 - 201 반환")
    void 사용자_통계_조회_API_성공_201_반환() throws Exception {
        // given
        UserStatisticsResDto response = UserStatisticsResDto.create(
                UserStatisticsRecipeReportResDto.create(List.of()),
                List.of(),
                DifficultyLevel.LEVEL_3
        );
        given(userStatisticsService.getUserStatistics(1L, 10L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}/statistics/{statisticsId}", 1L, 10L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message")
                        .value("사용자 세끼 리포트 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.recipeReport.count").value(0))
                .andExpect(jsonPath("$.data.averageDifficultyLevel").value("LEVEL_3"));
        then(userStatisticsService).should().getUserStatistics(1L, 10L);
    }

    @Test
    @DisplayName("사용자 통계 조회 API 실패 - userId가 양수가 아님")
    void 사용자_통계_조회_API_실패_userId_양수_아님() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}/statistics/{statisticsId}", 0L, 10L))
                .andExpect(status().isBadRequest());
        then(userStatisticsService).shouldHaveNoInteractions();
    }
}
