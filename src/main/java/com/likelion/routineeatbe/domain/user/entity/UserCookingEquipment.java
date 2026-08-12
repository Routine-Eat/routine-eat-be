package com.likelion.routineeatbe.domain.user.entity;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "user_cooking_equipment")
public class UserCookingEquipment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooking_equipment_id", nullable = false)
    private CookingEquipment cookingEquipment;

    public static UserCookingEquipment createUserCookingEquipment(
            User user, CookingEquipment cookingEquipment
    ){
        return UserCookingEquipment.builder()
                .user(user)
                .cookingEquipment(cookingEquipment)
                .build();
    }

}
