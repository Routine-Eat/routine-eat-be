package com.likelion.routineeatbe.domain.cookingEquipment.repository;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CookingEquipmentRepository extends JpaRepository<CookingEquipment,Long> {
    List<CookingEquipment> findByNameContaining(String name);
}
