package com.likelion.routineeatbe.domain.userSearchHistory.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.userSearchHistory.dto.response.UserSearchHistoryResDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserSearchHistoryMapperTest {

    private final UserSearchHistoryMapper userSearchHistoryMapper =
            new UserSearchHistoryMapper();

    @Test
    @DisplayName("사용자 검색 기록 목록 응답 DTO 변환 성공")
    void 사용자_검색_기록_목록_응답_DTO_변환_성공() {
        // given
        List<String> searchHistoryList = List.of("브로콜리", "목이버섯", "오이");

        // when
        UserSearchHistoryResDto result =
                userSearchHistoryMapper.toUserSearchHistoryResDto(searchHistoryList);

        // then
        assertThat(result.searchHistoryList())
                .containsExactly("브로콜리", "목이버섯", "오이");
    }

    @Test
    @DisplayName("사용자 검색 기록이 없는 경우 빈 목록 응답 DTO 변환 성공")
    void 사용자_검색_기록이_없는_경우_빈_목록_응답_DTO_변환_성공() {
        // given
        List<String> searchHistoryList = List.of();

        // when
        UserSearchHistoryResDto result =
                userSearchHistoryMapper.toUserSearchHistoryResDto(searchHistoryList);

        // then
        assertThat(result.searchHistoryList()).isEmpty();
    }
}
