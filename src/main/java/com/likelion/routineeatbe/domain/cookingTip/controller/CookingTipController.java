package com.likelion.routineeatbe.domain.cookingTip.controller;

import com.likelion.routineeatbe.domain.cookingTip.dto.response.CookingTipInitResDto;
import com.likelion.routineeatbe.domain.cookingTip.service.CookingTipInitializationService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CookingTipController implements CookingTipControllerDocs {

    private final CookingTipInitializationService cookingTipInitializationService;

    @Override
    public ResponseEntity<GlobalResponse<CookingTipInitResDto>> initializeCookingTips() {
        CookingTipInitResDto result = cookingTipInitializationService.initialize();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "요리 팁 데이터 초기화를 성공했습니다.",
                        result
                ));
    }
}
