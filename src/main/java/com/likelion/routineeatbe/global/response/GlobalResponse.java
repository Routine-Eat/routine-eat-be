package com.likelion.routineeatbe.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "BaseResponse DTO", description = "공통 API 응답 형식")
public class GlobalResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    private Object code;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    public static <T> GlobalResponse<T> success(T data) {
        return new GlobalResponse<>(true, 200, "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> GlobalResponse<T> success(String message, T data) {
        return new GlobalResponse<>(true, 200, message, data);
    }

    public static <T> GlobalResponse<T> success(int code, String message, T data) {
        return new GlobalResponse<>(true, code, message, data);
    }

    public static <T> GlobalResponse<T> error(String code, String message) {
        return new GlobalResponse<>(false, code, message, null);
    }
}


