package com.likelion.routineeatbe.domain.cookingEquipment.controller;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.cookingEquipment.service.CookingEquipmentService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CookingEquipmentController implements CookingEquipmentControllerDocs {
    private final CookingEquipmentService cookingEquipmentService;

    @Override
    public GlobalResponse<List<CookingEquipmentResponse>> getCookingEquipments(String search){
        List<CookingEquipmentResponse> cookingEquipmentResponseList = cookingEquipmentService.getCookingEquipments(search);
        return GlobalResponse.success("조리도구 조회가 성공했습니다.",cookingEquipmentResponseList);
    }
}
