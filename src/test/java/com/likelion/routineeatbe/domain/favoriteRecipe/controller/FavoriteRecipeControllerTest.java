package com.likelion.routineeatbe.domain.favoriteRecipe.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.likelion.routineeatbe.domain.favoriteRecipe.dto.request.FavoriteRecipeSearchReqDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeListResDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeResDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.service.FavoriteRecipeService;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import java.util.List;
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
        String userNumber = "1234";

        // when & then
        mockMvc.perform(delete("/api/v1/recipes/{recipeId}/favorites", recipeId)
                        .param("userNumber", userNumber))
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
    @DisplayName("레시피 찜 해제 API 실패 - 숫자 4자리가 아닌 userNumber")
    void 레시피_찜_해제_API_실패_숫자_4자리가_아닌_userNumber() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/v1/recipes/{recipeId}/favorites", 10L)
                        .param("userNumber", "0"))
                .andExpect(status().isBadRequest());
        then(favoriteRecipeService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("찜한 레시피 조회 API 성공 - 200 반환")
    void 찜한_레시피_조회_API_성공_200_반환() throws Exception {
        // given
        FavoriteRecipeSearchReqDto request = new FavoriteRecipeSearchReqDto(
                "1234",
                null,
                null
        );
        FavoriteRecipeResDto favoriteRecipe = FavoriteRecipeResDto.create(
                659L,
                "감자미역국",
                "http://example.com/thumbnail.jpg",
                20,
                DifficultyLevel.LEVEL_2,
                72L
        );
        FavoriteRecipeListResDto response = FavoriteRecipeListResDto.create(
                List.of(favoriteRecipe),
                true,
                11L
        );
        given(favoriteRecipeService.getFavoriteRecipes(request)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/recipes/favorites")
                        .param("userNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("찜한 레시피 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content[0].recipeId").value(659))
                .andExpect(jsonPath("$.data.content[0].menuName").value("감자미역국"))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl")
                        .value("http://example.com/thumbnail.jpg"))
                .andExpect(jsonPath("$.data.content[0].timeRequired").value(20))
                .andExpect(jsonPath("$.data.content[0].difficultyLevel").value("LEVEL_2"))
                .andExpect(jsonPath("$.data.content[0].foodIngredientUsingPercent").value(72))
                .andExpect(jsonPath("$.data.content[0].calory").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].category").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].matchedIngredientCount").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].requiredIngredientCount").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].requiredIngredientCost").doesNotExist())
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(11))
                .andExpect(jsonPath("$.data.size").doesNotExist());
        then(favoriteRecipeService).should().getFavoriteRecipes(request);
    }

    @Test
    @DisplayName("찜한 레시피 조회 API 실패 - userNumber 누락")
    void 찜한_레시피_조회_API_실패_userNumber_누락() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/recipes/favorites"))
                .andExpect(status().isBadRequest());
        then(favoriteRecipeService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("찜한 레시피 조회 API 실패 - 조회 크기 초과")
    void 찜한_레시피_조회_API_실패_조회_크기_초과() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/recipes/favorites")
                        .param("userNumber", "1234")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
        then(favoriteRecipeService).shouldHaveNoInteractions();
    }
}
