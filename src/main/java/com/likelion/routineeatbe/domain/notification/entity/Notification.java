package com.likelion.routineeatbe.domain.notification.entity;

import com.likelion.routineeatbe.domain.notification.enums.NotificationType;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification")
@AttributeOverrides({
        @AttributeOverride(
                name = "createdAt",
                column = @Column(name = "created_at", nullable = false, updatable = false)
        ),
        @AttributeOverride(
                name = "updatedAt",
                column = @Column(name = "updated_at", nullable = false)
        )
})
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 500)
    private String content;

    @Column(name = "content_id")
    private Long contentId;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static Notification create(
            User user,
            NotificationType type,
            Long contentId
    ) {
        return Notification.builder()
                .user(user)
                .type(type)
                .title(type.getTitle())
                .content(type.getContent())
                .contentId(contentId)
                .build();
    }

    public void markAsRead() {
        if (isRead) {
            return;
        }

        isRead = true;
        readAt = LocalDateTime.now();
    }
}
