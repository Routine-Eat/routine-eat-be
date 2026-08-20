package com.likelion.routineeatbe.domain.favoriteRecipe.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@JsonPropertyOrder({"content", "hasNext", "nextCursor"})
@Schema(title = "FavoriteRecipeListResDto", description = "찜한 레시피 커서 조회 응답 DTO")
public record FavoriteRecipeListResDto(
        @Schema(description = "조회된 찜 레시피 목록")
        List<FavoriteRecipeResDto> content,
        @Schema(description = "다음 데이터 존재 여부")
        boolean hasNext,
        @Schema(description = "다음 조회에 사용할 위치 커서")
        Long nextCursor
) {

    public static FavoriteRecipeListResDto create(
            List<FavoriteRecipeResDto> content,
            boolean hasNext,
            Long nextCursor
    ) {
        return FavoriteRecipeListResDto.builder()
                .content(content)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
