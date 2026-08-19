package com.likelion.routineeatbe.domain.userStatistics.repository;

import com.likelion.routineeatbe.domain.userStatistics.entity.UserStatistics;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {

    /**
     * 사용자 소유의 통계 단건을 조회합니다.
     *
     * @param statisticsId 통계 PK
     * @param userId 사용자 PK
     * @return 사용자 소유 통계
     */
    Optional<UserStatistics> findByIdAndUser_Id(Long statisticsId, Long userId);
}
