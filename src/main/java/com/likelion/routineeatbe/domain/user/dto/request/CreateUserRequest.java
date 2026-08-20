package com.likelion.routineeatbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


@Schema(title = "CreateUserRequest: 사용자 생성 요청 DTO")
public record CreateUserRequest(
        @NotBlank(message = "인증 번호 입력 필수")
        @Schema(description = "로그인용 고유 인증번호", example = "1234")
        @Valid
        String loginNumber
) { }
