package com.likelion.routineeatbe.domain.cookingRecord.repository;

import com.likelion.routineeatbe.domain.cookingRecord.dto.CookingRecordSearchResult;
import org.springframework.data.domain.Slice;

public interface CookingRecordRepositoryCustom {

    /**
     * 사용자의 회고 저장까지 종료된 요리 기록을 최신순으로 조회합니다.
     *
     * @param userId 조회할 사용자 PK
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 요리 기록 개수
     * @return 요리 기록 목록 조회 결과 Slice
     */
    Slice<CookingRecordSearchResult> searchTerminatedCookingRecords(
            Long userId,
            Integer cursor,
            Integer size
    );
}
