package com.likelion.routineeatbe.domain.cookingEquipment.entity;

import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "cooking_equipment")
public class CookingEquipment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(nullable = false,comment = "조리 도구 종류")
    @Enumerated(EnumType.STRING)
    private CookingEquipmentType type;

    @Column(comment = "대표 분야")
    @Enumerated(EnumType.STRING)
    private CookingEquipmentSymbol symbol;
}
