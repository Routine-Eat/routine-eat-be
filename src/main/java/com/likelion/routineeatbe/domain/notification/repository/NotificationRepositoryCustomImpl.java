package com.likelion.routineeatbe.domain.notification.repository;

import com.likelion.routineeatbe.domain.notification.entity.Notification;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final EntityManager entityManager;

    /**
     * 사용자 알림을 생성일과 알림 PK 내림차순으로 위치 커서 조회합니다.
     *
     * @param userNumber 사용자 고유 식별번호
     * @param cursor 1부터 시작하는 조회 위치
     * @param size 한 번에 조회할 알림 개수
     * @return 알림 목록 Slice
     */
    @Override
    public Slice<Notification> searchByUserNumber(
            String userNumber,
            Integer cursor,
            Integer size
    ) {
        List<Notification> content = new ArrayList<>(entityManager.createQuery("""
                        select notification
                        from Notification notification
                        join fetch notification.user user
                        where user.loginNumber = :userNumber
                        order by notification.createdAt desc, notification.id desc
                        """, Notification.class)
                .setParameter("userNumber", userNumber)
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
