package com.likelion.routineeatbe.domain.userSearchHistory.mapper;

import com.likelion.routineeatbe.domain.userSearchHistory.dto.response.UserSearchHistoryResDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserSearchHistoryMapper {

    /**
     * 중복이 제거된 최신 검색어 목록을 최근 검색 기록 응답 DTO로 변환합니다.
     * @param searchHistoryList 중복이 제거된 최신순 검색어 목록
     * @return 검색어 문자열 목록이 포함된 최근 검색 기록 응답 DTO
     */
    public UserSearchHistoryResDto toUserSearchHistoryResDto(
            List<String> searchHistoryList
    ) {
        return UserSearchHistoryResDto.create(searchHistoryList);
    }
}
