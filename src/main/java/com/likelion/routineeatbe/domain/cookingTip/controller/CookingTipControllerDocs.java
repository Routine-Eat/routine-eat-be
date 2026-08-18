package com.likelion.routineeatbe.domain.cookingTip.controller;

import com.likelion.routineeatbe.domain.cookingTip.dto.response.CookingTipInitResDto;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Cooking Tip", description = "요리 팁 관리 API")
@RequestMapping("/api/v1/cooking-tips")
public interface CookingTipControllerDocs {

    @Operation(
            summary = "요리 팁 초기화",
            description = """
                    서버의 요리 팁 SQL 시드 파일을 실행하여 요리 팁과 콘텐츠를 초기화합니다.

                    - 기존 요리 팁은 제목을 기준으로 갱신합니다.
                    - 시드에 포함된 팁의 기존 콘텐츠는 SQL 파일의 내용과 순서로 동기화합니다.
                    - 이미지 콘텐츠 URL은 실제 요리 팁 ID와 콘텐츠 ID를 사용해 생성합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "요리 팁 초기화 성공",
                    content = @Content(
                            schema = @Schema(implementation = CookingTipInitResDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "요리 팁 초기화 중 서버 오류",
                    content = @Content
            )
    })
    @PostMapping("/init")
    ResponseEntity<GlobalResponse<CookingTipInitResDto>> initializeCookingTips();
}
