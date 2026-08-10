package com.likelion.routineeatbe.domain.menu.controller;

import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeFoodIngredientResDto;
import com.likelion.routineeatbe.domain.menu.service.InitMenuAndRecipeFoodIngredientService;
import com.likelion.routineeatbe.domain.menu.service.MenuAndRecipeCrawlingService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MenuController implements MenuControllerDocs {

    private final MenuAndRecipeCrawlingService menuAndRecipeCrawlingService;
    private final InitMenuAndRecipeFoodIngredientService initMenuAndRecipeFoodIngredientService;

    @Override
    public GlobalResponse<Void> crawlFoodSafetyKorea(Integer startIdx, Integer endIdx) {
        menuAndRecipeCrawlingService.crawlAndSave(startIdx, endIdx);
        return GlobalResponse.success("식품안전청 메뉴와 레시피 데이터가 성공적으로 저장되었습니다.");
    }

    @Override
    public ResponseEntity<GlobalResponse<InitMenuAndRecipeFoodIngredientResDto>>
            initMenuAndRecipeFoodIngredients() {
        InitMenuAndRecipeFoodIngredientResDto result = initMenuAndRecipeFoodIngredientService.initialize();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "메뉴/레시피별 필요 음식 재료 데이터 초기화를 성공했습니다.",
                        result
                ));
    }
}
