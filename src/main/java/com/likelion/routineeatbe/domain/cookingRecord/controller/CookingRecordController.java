package com.likelion.routineeatbe.domain.cookingRecord.controller;

import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingResultSaveReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingAiReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingAiResult;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingRecordSearchReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingSessionLogSearchReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.request.CookingStartReqDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordDetailResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingCompleteResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordFoodIngredientsResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordInProgressResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingRecordStepTitlesResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingSessionLogListResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingResultSaveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStartResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CurrentCookingStepResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepMoveResDto;
import com.likelion.routineeatbe.domain.cookingRecord.dto.response.CookingStepNavigationResDto;
import com.likelion.routineeatbe.domain.cookingRecord.service.CookingRecordService;
import com.likelion.routineeatbe.domain.cookingRecord.service.CookingAiService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
public class CookingRecordController implements CookingRecordControllerDocs {

    private final CookingRecordService cookingRecordService;
    private final CookingAiService cookingAiService;

    @Override
    public ResponseEntity<GlobalResponse<CookingRecordInProgressResDto>>
            getInProgressCookingRecord(String userNumber) {
        CookingRecordInProgressResDto result = cookingRecordService
                .getInProgressCookingRecord(userNumber);
        return ResponseEntity.ok(GlobalResponse.success(
                HttpStatus.CREATED.value(),
                "해당 사용자가 진행 중인 요리 세션 조회에 성공했습니다.",
                result
        ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CookingRecordStepTitlesResDto>> getInProgressCookingStepTitles(
            String userNumber
    ) {
        CookingRecordStepTitlesResDto result = cookingRecordService
                .getInProgressCookingStepTitles(userNumber);
        return ResponseEntity.ok(GlobalResponse.success(
                HttpStatus.CREATED.value(),
                "요리 전체 단계 조회에 성공했습니다.",
                result
        ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CookingRecordListResDto>> getCookingRecords(
            CookingRecordSearchReqDto request
    ) {
        CookingRecordListResDto result = cookingRecordService.getCookingRecords(request);
        return ResponseEntity.ok(GlobalResponse.success(
                HttpStatus.OK.value(),
                "요리 기록(회고록) 조회에 성공했습니다.",
                result
        ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CookingRecordDetailResDto>> getCookingRecordDetail(
            Long cookingRecordId,
            String userNumber
    ) {
        CookingRecordDetailResDto result = cookingRecordService.getCookingRecordDetail(
                cookingRecordId,
                userNumber
        );
        return ResponseEntity.ok(GlobalResponse.success(
                HttpStatus.OK.value(),
                "요리 기록(회고록) 상세 조회에 성공하였습니다.",
                result
        ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CookingRecordFoodIngredientsResDto>> getFoodIngredients(
            Long cookingRecordId,
            String userNumber
    ) {
        CookingRecordFoodIngredientsResDto result = cookingRecordService.getFoodIngredients(
                cookingRecordId,
                userNumber
        );
        return ResponseEntity.ok(GlobalResponse.success(
                HttpStatus.OK.value(),
                "성공했습니다.",
                result
        ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CookingResultSaveResDto>> saveCookingResult(
            String userNumber,
            CookingResultSaveReqDto request,
            MultipartFile image
    ) {
        CookingResultSaveResDto result = cookingRecordService.saveCookingResult(
                userNumber,
                request,
                image
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "성공했습니다.",
                        result
                ));
    }

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
    public ResponseEntity<GlobalResponse<CurrentCookingStepResDto>> getCurrentCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        CurrentCookingStepResDto result = cookingRecordService.getCurrentCookingStep(
                cookingRecordId,
                userNumber
        );
        return ResponseEntity.ok(GlobalResponse.success(
                HttpStatus.CREATED.value(),
                "현재 %d번째 단계입니다."
                        .formatted(result.currentCookingStep().level()),
                result
        ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CurrentCookingStepResDto>> moveToLastCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        CurrentCookingStepResDto result = cookingRecordService.moveToLastCookingStep(
                cookingRecordId,
                userNumber
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        HttpStatus.CREATED.value(),
                        "마지막 요리 단계로 이동했습니다. 현재 %d번째 단계입니다."
                                .formatted(result.currentCookingStep().level()),
                        result
                ));
    }

    @Override
    public ResponseEntity<GlobalResponse<CookingStepMoveResDto>> moveToNextCookingStep(
            Long cookingRecordId,
            String userNumber
    ) {
        CookingStepMoveResDto result = cookingRecordService.moveToNextCookingStep(
                cookingRecordId,
                userNumber
        );
        String message = result instanceof CookingCompleteResDto
                ? "요리가 종료되었습니다."
                : "다음 요리 단계로 이동했습니다. 현재 %d번째 단계입니다."
                        .formatted(((CookingStepNavigationResDto) result)
                                .currentCookingStep().level());
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

    @Override
    public ResponseEntity<GlobalResponse<CookingSessionLogListResDto>> getCookingSessionLogs(
            Long cookingRecordId,
            CookingSessionLogSearchReqDto request
    ) {
        CookingSessionLogListResDto result = cookingRecordService.getCookingSessionLogs(
                cookingRecordId,
                request
        );
        return ResponseEntity.ok(GlobalResponse.success(
                HttpStatus.OK.value(),
                "AI 대화 기록 조회에 성공했습니다.",
                result
        ));
    }

    @Override
    public ResponseEntity<MultiValueMap<String, HttpEntity<?>>> requestCookingAi(
            Long cookingRecordId,
            String userNumber,
            CookingAiReqDto request
    ) {
        CookingAiResult result = cookingAiService.interact(
                cookingRecordId,
                userNumber,
                request
        );
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part(
                        "response",
                        GlobalResponse.success(
                                HttpStatus.CREATED.value(),
                                result.message(),
                                result.data()
                        )
                )
                .contentType(MediaType.APPLICATION_JSON);
        if (result.audio() != null) {
            ByteArrayResource audioResource = new ByteArrayResource(result.audio()) {
                @Override
                public String getFilename() {
                    return "cooking-ai-answer.wav";
                }
            };
            bodyBuilder.part("audio", audioResource)
                    .contentType(MediaType.parseMediaType("audio/wav"));
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build());
    }
}
