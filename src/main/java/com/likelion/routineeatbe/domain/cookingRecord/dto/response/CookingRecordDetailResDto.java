package com.likelion.routineeatbe.domain.cookingRecord.dto.response;

import com.likelion.routineeatbe.domain.cookingRecord.enums.TasteRating;
import com.likelion.routineeatbe.domain.menu.entity.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(title = "CookingRecordDetailResDto", description = "요리 기록 상세 조회 응답 DTO")
public record CookingRecordDetailResDto(
        @Schema(description = "요리 기록 PK", example = "1")
        Long cookingRecordId,
        @Schema(description = "메뉴 이름", example = "감자미역국")
        String menuName,
        @Schema(description = "메뉴 썸네일 이미지 URL")
        String thumbnailUrl,
        @Schema(description = "메뉴 조리 소요 시간(분)", example = "20")
        Integer timeRequired,
        @Schema(description = "메뉴에 저장된 요리 난이도", example = "LEVEL_2")
        DifficultyLevel difficultyLevel,
        @Schema(description = "사용자가 평가한 맛 점수", example = "LEVEL_1")
        TasteRating userTasteRating,
        @Schema(description = "사용자가 평가한 실제 요리 난이도", example = "LEVEL_2")
        DifficultyLevel userDifficultyLevel,
        @Schema(description = "사용자가 작성한 요리 팁", example = "참기름을 조금 더 넣으면 맛있습니다.")
        String cookingTip,
        @Schema(description = "사용자가 저장한 요리 기록 사진 URL")
        String userCookingRecordPhotoUrl
) {

    public static CookingRecordDetailResDto create(
            Long cookingRecordId,
            String menuName,
            String thumbnailUrl,
            Integer timeRequired,
            DifficultyLevel difficultyLevel,
            TasteRating userTasteRating,
            DifficultyLevel userDifficultyLevel,
            String cookingTip,
            String userCookingRecordPhotoUrl
    ) {
        return CookingRecordDetailResDto.builder()
                .cookingRecordId(cookingRecordId)
                .menuName(menuName)
                .thumbnailUrl(thumbnailUrl)
                .timeRequired(timeRequired)
                .difficultyLevel(difficultyLevel)
                .userTasteRating(userTasteRating)
                .userDifficultyLevel(userDifficultyLevel)
                .cookingTip(cookingTip)
                .userCookingRecordPhotoUrl(userCookingRecordPhotoUrl)
                .build();
    }
}
