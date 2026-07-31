package com.likelion.routineeatbe.global.common;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass //해당 클래스를 테이블로 생성하지 않고, 상속받은 자식 엔티티에 필드가 포함
@EntityListeners(AuditingEntityListener.class) //엔티티의 생성·수정 이벤트를 감지하여 Auditing 기능이 자동 동작하도록 설정
public abstract class BaseTimeEntity {

    @CreatedDate //엔티티가 처음 저장될 때 현재 시간을 자동으로 저장
    private LocalDateTime createdAt;

    @LastModifiedDate //엔티티가 수정될 때 현재 시간을 자동으로 갱신
    private LocalDateTime updatedAt;
}

