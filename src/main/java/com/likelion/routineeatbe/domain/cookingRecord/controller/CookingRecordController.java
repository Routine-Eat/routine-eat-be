package com.likelion.routineeatbe.domain.cookingRecord.controller;

import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
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
}
