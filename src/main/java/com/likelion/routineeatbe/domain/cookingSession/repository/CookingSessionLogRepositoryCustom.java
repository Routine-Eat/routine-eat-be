package com.likelion.routineeatbe.domain.cookingSession.repository;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSessionLog;
import org.springframework.data.domain.Slice;

public interface CookingSessionLogRepositoryCustom {

    /**
     * 요리 세션에 저장된 로그를 생성 순서대로 위치 커서 조회합니다.
     *
     * @param cookingSessionId 조회할 요리 세션 PK
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 요리 세션 로그 개수
     * @return 요리 세션 로그 조회 결과 Slice
     */
    Slice<CookingSessionLog> searchByCookingSessionId(
            Long cookingSessionId,
            Integer cursor,
            Integer size
    );
}
