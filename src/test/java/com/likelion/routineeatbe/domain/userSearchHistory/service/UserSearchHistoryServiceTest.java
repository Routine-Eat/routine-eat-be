package com.likelion.routineeatbe.domain.userSearchHistory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.domain.userSearchHistory.dto.request.UserSearchHistoryReqDto;
import com.likelion.routineeatbe.domain.userSearchHistory.dto.response.UserSearchHistoryResDto;
import com.likelion.routineeatbe.domain.userSearchHistory.mapper.UserSearchHistoryMapper;
import com.likelion.routineeatbe.domain.userSearchHistory.repository.UserSearchHistoryRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class UserSearchHistoryServiceTest {

    @InjectMocks
    private UserSearchHistoryService userSearchHistoryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSearchHistoryRepository userSearchHistoryRepository;

    @Mock
    private UserSearchHistoryMapper userSearchHistoryMapper;

    @Test
    @DisplayName("사용자 최근 검색 기록 최신순 조회 성공")
    void 사용자_최근_검색_기록_최신순_조회_성공() {
        // given
        UserSearchHistoryReqDto request = new UserSearchHistoryReqDto("1234");
        User user = User.builder().id(1L).loginNumber("1234").build();
        List<String> searchHistoryList = List.of("브로콜리", "목이버섯", "오이");
        UserSearchHistoryResDto response = UserSearchHistoryResDto.create(
                List.of("브로콜리", "목이버섯", "오이")
        );
        given(userRepository.findByLoginNumber("1234")).willReturn(Optional.of(user));
        given(userSearchHistoryRepository.findDistinctContentsByUserId(
                1L,
                PageRequest.of(0, 5)
        )).willReturn(searchHistoryList);
        given(userSearchHistoryMapper.toUserSearchHistoryResDto(searchHistoryList))
                .willReturn(response);

        // when
        UserSearchHistoryResDto result = userSearchHistoryService.getSearchHistories(request);

        // then
        assertThat(result.searchHistoryList())
                .containsExactly("브로콜리", "목이버섯", "오이");
        verify(userSearchHistoryRepository)
                .findDistinctContentsByUserId(user.getId(), PageRequest.of(0, 5));
        verify(userSearchHistoryMapper).toUserSearchHistoryResDto(searchHistoryList);
    }

    @Test
    @DisplayName("최근 검색 기록 조회 실패 - 존재하지 않는 사용자")
    void 최근_검색_기록_조회_실패_존재하지_않는_사용자() {
        // given
        UserSearchHistoryReqDto request = new UserSearchHistoryReqDto("9999");
        given(userRepository.findByLoginNumber("9999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userSearchHistoryService.getSearchHistories(request))
                .isInstanceOf(CustomException.class);
        verify(userSearchHistoryRepository, never())
                .findDistinctContentsByUserId(1L, PageRequest.of(0, 5));
    }
}
