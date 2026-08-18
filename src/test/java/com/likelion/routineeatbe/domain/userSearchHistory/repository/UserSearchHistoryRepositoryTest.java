package com.likelion.routineeatbe.domain.userSearchHistory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.userSearchHistory.entity.UserSearchHistory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-search-history-test;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserSearchHistoryRepositoryTest {

    @Autowired
    private UserSearchHistoryRepository userSearchHistoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("사용자 검색 기록 저장 성공")
    void 사용자_검색_기록_저장_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1234").build());

        // when
        UserSearchHistory result = userSearchHistoryRepository.saveAndFlush(
                UserSearchHistory.create(user, "감자")
        );

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getContent()).isEqualTo("감자");
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("동일한 사용자의 중복 검색어 저장 성공")
    void 동일한_사용자의_중복_검색어_저장_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("5678").build());

        // when
        userSearchHistoryRepository.save(UserSearchHistory.create(user, "감자"));
        userSearchHistoryRepository.saveAndFlush(UserSearchHistory.create(user, "감자"));

        // then
        assertThat(userSearchHistoryRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("사용자 최근 검색 기록 5개 최신순 조회 성공")
    void 사용자_최근_검색_기록_5개_최신순_조회_성공() {
        // given
        User targetUser = entityManager.persist(User.builder().loginNumber("1111").build());
        User otherUser = entityManager.persist(User.builder().loginNumber("2222").build());
        for (int index = 1; index <= 6; index++) {
            userSearchHistoryRepository.saveAndFlush(
                    UserSearchHistory.create(targetUser, "검색어" + index)
            );
        }
        userSearchHistoryRepository.saveAndFlush(
                UserSearchHistory.create(targetUser, "검색어3")
        );
        userSearchHistoryRepository.saveAndFlush(
                UserSearchHistory.create(otherUser, "다른 사용자 검색어")
        );
        entityManager.clear();

        // when
        List<String> result =
                userSearchHistoryRepository.findDistinctContentsByUserId(
                        targetUser.getId(),
                        PageRequest.of(0, 5)
                );

        // then
        assertThat(result)
                .containsExactly("검색어3", "검색어6", "검색어5", "검색어4", "검색어2");
    }
}
