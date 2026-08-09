package com.likelion.routineeatbe.domain.user.dto.response;

import com.likelion.routineeatbe.domain.user.entity.SkillLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(title = "UserResponse: 사용자 응답 DTO")
public class UserResponse {
    @Schema(description = "사용자 식별 id", example = "1")
    private Long id;

    @Schema(description = "사용자 로그인 번호",example = "1234")
    private String loginNumber;

    @Schema(description = "사용자 요리실력",example = "BEGGINER")
    private SkillLevel skillLevel;

    @Schema(description = "사용자 생성날짜",example = "2026-00-00T00:00:00")
    private LocalDateTime createdAt;
}
