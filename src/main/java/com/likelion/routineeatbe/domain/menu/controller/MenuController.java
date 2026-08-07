package com.likelion.routineeatbe.domain.menu.controller;

import com.likelion.routineeatbe.domain.menu.service.MenuAndRecipeCrawlingService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MenuController implements MenuControllerDocs {

    private final MenuAndRecipeCrawlingService menuAndRecipeCrawlingService;

    @Override
    public GlobalResponse<Void> crawlFoodSafetyKorea(Integer startIdx, Integer endIdx) {
        menuAndRecipeCrawlingService.crawlAndSave(startIdx, endIdx);
        return GlobalResponse.success("식품안전청 메뉴와 레시피 데이터가 성공적으로 저장되었습니다.");
    }
}
