package com.likelion.routineeatbe.domain.user.dto.response;

import com.likelion.routineeatbe.domain.user.entity.SkillLevel;
import com.likelion.routineeatbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Schema(title = "UserResponse: 사용자 응답 DTO")
public record UserResponse(
        @Schema(description = "사용자 식별 id", example = "1")
        Long id,

        @Schema(description = "사용자 로그인 번호",example = "1234")
        String loginNumber,

        @Schema(description = "사용자 요리실력",example = "BEGGINER")
        SkillLevel skillLevel,

        @Schema(description = "사용자 생성날짜",example = "2026-00-00T00:00:00")
        LocalDateTime createdAt
){
    /* return값 UserResponse 포장 함수 */
    public static UserResponse fromUserEntity(User user){
        return UserResponse.builder()
                .id(user.getId())
                .loginNumber(user.getLoginNumber())
                .skillLevel(user.getSkillLevel())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
