package com.likelion.routineeatbe.domain.userStatistics.controller;

import com.likelion.routineeatbe.domain.userStatistics.dto.response.UserStatisticsResDto;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User Statistics", description = "사용자 통계 조회 API")
@RequestMapping("/api/v1/users")
public interface UserStatisticsControllerDocs {

    @Operation(
            summary = "사용자 통계 조회",
            description = "사용자 PK와 통계 PK로 사용자 세끼 리포트를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "사용자 세끼 리포트 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserStatisticsResDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "사용자 또는 통계를 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping("/{userId}/statistics/{statisticsId}")
    ResponseEntity<GlobalResponse<UserStatisticsResDto>> getUserStatistics(
            @Parameter(description = "사용자 PK", required = true)
            @Positive @PathVariable("userId") Long userId,
            @Parameter(description = "통계 PK", required = true)
            @Positive @PathVariable("statisticsId") Long statisticsId
    );
}
