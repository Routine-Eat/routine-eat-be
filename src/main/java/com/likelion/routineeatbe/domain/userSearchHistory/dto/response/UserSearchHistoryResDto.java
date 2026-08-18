package com.likelion.routineeatbe.domain.userSearchHistory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(title = "UserSearchHistoryResDto", description = "최근 검색 기록 조회 응답 DTO")
public record UserSearchHistoryResDto(
        @Schema(description = "최신순 검색 기록 목록", example = "[\"브로콜리\", \"목이버섯\", \"오이\"]")
        List<String> searchHistoryList
) {

    public static UserSearchHistoryResDto create(List<String> searchHistoryList) {
        return UserSearchHistoryResDto.builder()
                .searchHistoryList(searchHistoryList)
                .build();
    }
}
