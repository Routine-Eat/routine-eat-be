package com.likelion.routineeatbe.domain.cookingEquipment.dto.response;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipmentSymbol;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingEquipmentResponse: 조리도구 응답 DTO")
public record CookingEquipmentResponse(
        @Schema(description = "조리도구 식별 id",example = "1")
        Long cookingEquipmentId,

        @Schema(description = "조리도구 이름",example = "칼")
        String cookingEquipmentName,

        @Schema(description = "조리도구 종류",example = "PREP_TOOL")
        CookingEquipmentType cookingEquipmentType,

        @Schema(description = "대표 분야",example = "ESSENTIAL")
        CookingEquipmentSymbol cookingEquipmentSymbol
) {
    public static CookingEquipmentResponse from(CookingEquipment cookingEquipment){
        return CookingEquipmentResponse.builder()
                .cookingEquipmentId(cookingEquipment.getId())
                .cookingEquipmentName(cookingEquipment.getName())
                .cookingEquipmentType(cookingEquipment.getType())
                .cookingEquipmentSymbol(cookingEquipment.getSymbol())
                .build();
    }
}
