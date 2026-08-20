package com.likelion.routineeatbe.domain.notification.controller;

import com.likelion.routineeatbe.domain.notification.dto.response.NotificationPollingResDto;
import com.likelion.routineeatbe.domain.notification.dto.request.NotificationSearchReqDto;
import com.likelion.routineeatbe.domain.notification.dto.response.NotificationListResDto;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;

@Validated
@Tag(name = "Notification", description = "알림 API")
@RequestMapping("/api/v1/notifications")
public interface NotificationControllerDocs {

    @Operation(
            summary = "신규 알림 조회",
            description = "사용자의 읽지 않은 알림을 Polling 방식으로 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "신규 알림 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationPollingResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 사용자 식별번호", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/polling")
    ResponseEntity<GlobalResponse<NotificationPollingResDto>> pollNotifications(
            @Parameter(description = "사용자 고유 식별번호", required = true)
            @NotBlank(message = "사용자 고유 식별번호는 필수입니다.")
            @Size(min = 4, max = 4, message = "사용자 고유 식별번호는 4자리여야 합니다.")
            @Pattern(regexp = "^[0-9]{4}$", message = "사용자 고유 식별번호는 숫자 4자리여야 합니다.")
            @RequestParam("userNumber") String userNumber
    );

    @Operation(
            summary = "알림 목록 조회",
            description = "사용자의 알림 목록을 최신순으로 커서 기반 무한 스크롤 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "알림 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = NotificationListResDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 조회 조건", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping
    ResponseEntity<GlobalResponse<NotificationListResDto>> getNotifications(
            @Valid @ModelAttribute NotificationSearchReqDto request
    );
}
