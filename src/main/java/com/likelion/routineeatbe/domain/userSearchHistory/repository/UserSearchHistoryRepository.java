package com.likelion.routineeatbe.domain.userSearchHistory.repository;

import com.likelion.routineeatbe.domain.userSearchHistory.entity.UserSearchHistory;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSearchHistoryRepository extends JpaRepository<UserSearchHistory, Long> {

    /**
     * 사용자 PK를 기준으로 중복 검색어를 제거하고 각 검색어의 최근 기록 순으로 조회합니다.
     * @param userId 사용자 PK
     * @param pageable 조회 개수 제한 정보
     * @return 중복이 제거된 최신순 검색어 목록
     */
    @Query("""
            select searchHistory.content
            from UserSearchHistory searchHistory
            where searchHistory.user.id = :userId
            group by searchHistory.content
            order by max(searchHistory.createdAt) desc, max(searchHistory.id) desc
            """)
    List<String> findDistinctContentsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
