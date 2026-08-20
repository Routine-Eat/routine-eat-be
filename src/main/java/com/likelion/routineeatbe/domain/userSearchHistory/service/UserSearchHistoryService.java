package com.likelion.routineeatbe.domain.userSearchHistory.service;

import com.likelion.routineeatbe.domain.recipe.exception.RecipeErrorCode;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.domain.userSearchHistory.dto.request.UserSearchHistoryReqDto;
import com.likelion.routineeatbe.domain.userSearchHistory.dto.response.UserSearchHistoryResDto;
import com.likelion.routineeatbe.domain.userSearchHistory.mapper.UserSearchHistoryMapper;
import com.likelion.routineeatbe.domain.userSearchHistory.repository.UserSearchHistoryRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchHistoryService {

    private static final int RECENT_SEARCH_HISTORY_LIMIT = 5;

    private final UserRepository userRepository;
    private final UserSearchHistoryRepository userSearchHistoryRepository;
    private final UserSearchHistoryMapper userSearchHistoryMapper;

    /**
     * 사용자 고유 식별번호를 기준으로 중복을 제거한 최근 검색어 5개를 최신순으로 조회합니다.
     * - 사용자가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
     * - 저장된 검색 기록이 없으면 빈 목록을 반환합니다.
     * @param request 사용자 고유 식별번호
     * @return 최신순 검색어 목록이 포함된 응답 DTO
     */
    @Transactional(readOnly = true)
    public UserSearchHistoryResDto getSearchHistories(UserSearchHistoryReqDto request) {
        log.info(
                "[UserSearchHistoryService] 최근 검색 기록 조회 | getSearchHistories() - START | userNumber: {}",
                request.userNumber()
        );

        /*
            1. 사용자 존재 여부 확인
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(RecipeErrorCode.USER_NOT_FOUND));

        /*
            2. 최근 검색어 조회
            - 동일한 검색어는 가장 최근 기록 하나로 집계하고 최신 검색어를 최대 5개 조회합니다.
         */
        List<String> searchHistoryList =
                userSearchHistoryRepository.findDistinctContentsByUserId(
                        user.getId(),
                        PageRequest.of(0, RECENT_SEARCH_HISTORY_LIMIT)
                );

        /*
            3. 최근 검색 기록 응답 변환
            - 중복이 제거된 검색어 문자열 목록을 응답 DTO로 변환합니다.
         */
        UserSearchHistoryResDto result =
                userSearchHistoryMapper.toUserSearchHistoryResDto(searchHistoryList);

        log.info(
                "[UserSearchHistoryService] 최근 검색 기록 조회 | getSearchHistories() - END | resultSize: {}",
                result.searchHistoryList().size()
        );
        return result;
    }
}
