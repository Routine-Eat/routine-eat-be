package com.likelion.routineeatbe.domain.favoriteRecipe.controller;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.routineeatbe.domain.favoriteRecipe.service.FavoriteRecipeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FavoriteRecipeController.class)
class FavoriteRecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteRecipeService favoriteRecipeService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("레시피 찜 API 성공 - 201 반환")
    void 레시피_찜_API_성공_201_반환() throws Exception {
        // given
        Long recipeId = 10L;
        Integer userNumber = 1234;

        // when & then
        mockMvc.perform(post("/api/v1/recipes/{recipeId}/favorites", recipeId)
                        .param("userNumber", userNumber.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("레시피 찜에 성공했습니다."));
        then(favoriteRecipeService).should().addFavorite(recipeId, userNumber);
    }

    @Test
    @DisplayName("레시피 찜 API 실패 - userNumber 누락")
    void 레시피_찜_API_실패_userNumber_누락() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/recipes/{recipeId}/favorites", 10L))
                .andExpect(status().isBadRequest());
        then(favoriteRecipeService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("레시피 찜 API 실패 - 양수가 아닌 recipeId")
    void 레시피_찜_API_실패_양수가_아닌_recipeId() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/recipes/{recipeId}/favorites", 0L)
                        .param("userNumber", "1234"))
                .andExpect(status().isBadRequest());
        then(favoriteRecipeService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("레시피 찜 해제 API 성공 - 201 반환")
    void 레시피_찜_해제_API_성공_201_반환() throws Exception {
        // given
        Long recipeId = 10L;
        Integer userNumber = 1234;

        // when & then
        mockMvc.perform(delete("/api/v1/recipes/{recipeId}/favorites", recipeId)
                        .param("userNumber", userNumber.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("레시피 찜 해제에 성공했습니다."));
        then(favoriteRecipeService).should().removeFavorite(recipeId, userNumber);
    }

    @Test
    @DisplayName("레시피 찜 해제 API 실패 - userNumber 누락")
    void 레시피_찜_해제_API_실패_userNumber_누락() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/recipes/{recipeId}/favorites", 10L))
                .andExpect(status().isBadRequest());
        then(favoriteRecipeService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("레시피 찜 해제 API 실패 - 양수가 아닌 userNumber")
    void 레시피_찜_해제_API_실패_양수가_아닌_userNumber() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/recipes/{recipeId}/favorites", 10L)
                        .param("userNumber", "0"))
                .andExpect(status().isBadRequest());
        then(favoriteRecipeService).shouldHaveNoInteractions();
    }
}
