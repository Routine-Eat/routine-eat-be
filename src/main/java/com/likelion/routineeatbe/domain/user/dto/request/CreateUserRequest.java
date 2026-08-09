package com.likelion.routineeatbe.domain.user.dto.request;

import com.likelion.routineeatbe.domain.user.entity.SkillLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(title = "CreateUserRequest: 사용자 생성 요청 DTO")
public class CreateUserRequest {

    @NotBlank(message = "인증 번호 입력 필수")
    @Schema(description = "로그인용 고유 인증번호", example = "1234")
    private String loginNumber;

}
