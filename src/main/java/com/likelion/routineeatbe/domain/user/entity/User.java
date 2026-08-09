package com.likelion.routineeatbe.domain.user.entity;

import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4, unique = true)
    private String loginNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SkillLevel skillLevel;

    public static User createUser(String loginNumber){
        return User.builder()
                .loginNumber(loginNumber)
                .build();
    }


}
