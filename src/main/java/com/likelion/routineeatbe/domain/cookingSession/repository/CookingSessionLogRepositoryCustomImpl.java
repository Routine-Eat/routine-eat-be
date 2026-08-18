package com.likelion.routineeatbe.domain.cookingSession.repository;

import com.likelion.routineeatbe.domain.cookingSession.entity.CookingSessionLog;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class CookingSessionLogRepositoryCustomImpl
        implements CookingSessionLogRepositoryCustom {

    private final EntityManager entityManager;

    /**
     * 요리 세션 로그를 PK 오름차순으로 위치 커서 조회합니다.
     * - size + 1건을 조회하여 다음 데이터 존재 여부를 판별합니다.
     *
     * @param cookingSessionId 조회할 요리 세션 PK
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 요리 세션 로그 개수
     * @return 요리 세션 로그 조회 결과 Slice
     */
    @Override
    public Slice<CookingSessionLog> searchByCookingSessionId(
            Long cookingSessionId,
            Integer cursor,
            Integer size
    ) {
        List<CookingSessionLog> content = new ArrayList<>(entityManager.createQuery("""
                        select cookingSessionLog
                        from CookingSessionLog cookingSessionLog
                        where cookingSessionLog.cookingSession.id = :cookingSessionId
                        order by cookingSessionLog.id asc
                        """, CookingSessionLog.class)
                .setParameter("cookingSessionId", cookingSessionId)
                .setFirstResult(cursor - 1)
                .setMaxResults(size + 1)
                .getResultList());

        boolean hasNext = content.size() > size;
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
    }
}
