package com.likelion.routineeatbe.domain.recipeCookingEquipment.repository;

import com.likelion.routineeatbe.domain.recipeCookingEquipment.entity.RecipeCookingEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeCookingEquipmentRepository
        extends JpaRepository<RecipeCookingEquipment, Long> {
}
