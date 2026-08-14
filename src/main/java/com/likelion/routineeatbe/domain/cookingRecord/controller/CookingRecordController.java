package com.likelion.routineeatbe.domain.cookingRecord.controller;

import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.service.CookingRecordService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class CookingRecordController implements CookingRecordControllerDocs {

    private final CookingRecordService cookingRecordService;

    @Override
    public ResponseEntity<GlobalResponse<CookingStartResDto>> startCooking(
            String userNumber,
            CookingStartReqDto request
    ) {
        CookingStartResDto result = cookingRecordService.startCooking(userNumber, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "요리 시작에 성공했습니다.",
                        result
                ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CookingStepNavigationResDto>> moveToNextCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        CookingStepNavigationResDto result = cookingRecordService.moveToNextCookingStep(
                cookingRecordId,
                userNumber
        );
        String message = result == null
                ? "요리가 종료되었습니다."
                : "다음 요리 단계로 이동했습니다. 현재 %d번째 단계입니다."
                        .formatted(result.currentCookingStep().level());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        message,
                        result
                ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CookingStepNavigationResDto>>
            moveToPreviousCookingStep(
                    Long cookingRecordId,
                    String userNumber
            ) {
        CookingStepNavigationResDto result = cookingRecordService.moveToPreviousCookingStep(
                cookingRecordId,
                userNumber
        );
        String message = result == null
                ? "1 이전 단계로 이동할 수 없습니다."
                : "이전 요리 단계로 이동했습니다. 현재 %d번째 단계입니다."
                        .formatted(result.currentCookingStep().level());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        message,
                        result
                ));
    }
}
