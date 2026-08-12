package com.likelion.routineeatbe.domain.favoriteRecipe.controller;

import com.likelion.routineeatbe.domain.favoriteRecipe.service.FavoriteRecipeService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class FavoriteRecipeController implements FavoriteRecipeControllerDocs {

    private final FavoriteRecipeService favoriteRecipeService;

    @Override
    public ResponseEntity<GlobalResponse<Void>> addFavorite(
            Long recipeId,
            Integer userNumber
    ) {
        favoriteRecipeService.addFavorite(recipeId, userNumber);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "레시피 찜에 성공했습니다.",
                        null
                ));
    }

    @Override
    public ResponseEntity<GlobalResponse<Void>> removeFavorite(
            Long recipeId,
            Integer userNumber
    ) {
        favoriteRecipeService.removeFavorite(recipeId, userNumber);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "레시피 찜 해제에 성공했습니다.",
                        null
                ));
    }
}
