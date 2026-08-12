package com.likelion.routineeatbe.domain.user.dto.request;

import com.likelion.routineeatbe.domain.user.entity.SkillLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(title = "UpdateUserRequest: 사용자 프로필 정보 수정 DTO")
public record UpdateUserRequest(
        @NotNull(message = "요리 실력 입력 필수")
        @Schema(description = "사용자 요리 실력",example = "PRO")
        SkillLevel skillLevel
) {
}
