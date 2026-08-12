package com.likelion.routineeatbe.domain.favoriteRecipe.service;

import com.likelion.routineeatbe.domain.favoriteRecipe.entity.FavoriteRecipe;
import com.likelion.routineeatbe.domain.favoriteRecipe.dto.request.FavoriteRecipeSearchReqDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.dto.response.FavoriteRecipeListResDto;
import com.likelion.routineeatbe.domain.favoriteRecipe.exception.FavoriteRecipeErrorCode;
import com.likelion.routineeatbe.domain.favoriteRecipe.mapper.FavoriteRecipeMapper;
import com.likelion.routineeatbe.domain.favoriteRecipe.repository.FavoriteRecipeRepository;
import com.likelion.routineeatbe.domain.recipe.entity.Recipe;
import com.likelion.routineeatbe.domain.recipe.dto.RecipeSearchResult;
import com.likelion.routineeatbe.domain.recipe.repository.RecipeRepository;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteRecipeService {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final FavoriteRecipeRepository favoriteRecipeRepository;
    private final FavoriteRecipeMapper favoriteRecipeMapper;

    /**
     * (1) 작업 목적
     * 사용자가 선택한 레시피를 찜 목록에 등록합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호와 레시피 PK로 등록 대상을 조회합니다.
     * - 동일한 사용자와 레시피의 찜이 존재하면 중복 찜 예외를 발생시킵니다.
     * - 찜 관계를 저장하고 복합 유니크 제약 위반도 중복 찜 예외로 변환합니다.
     *
     * @param recipeId 찜할 레시피 PK
     * @param userNumber 사용자 고유 식별번호
     */
    @Transactional
    public void addFavorite(Long recipeId, Integer userNumber) {
        log.info(
                "[FavoriteRecipeService] 레시피 찜 등록 | addFavorite() - START | recipeId: {}, userNumber: {}",
                recipeId,
                userNumber
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(String.valueOf(userNumber))
                .orElseThrow(() -> new CustomException(
                        FavoriteRecipeErrorCode.USER_NOT_FOUND
                ));

        /*
            2. 레시피 조회
            - 레시피 PK가 존재하지 않으면 RECIPE_NOT_FOUND 예외를 발생시킵니다.
         */
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(
                        FavoriteRecipeErrorCode.RECIPE_NOT_FOUND
                ));

        /*
            3. 중복 찜 확인
            - 동일한 사용자와 레시피의 찜이 이미 존재하면 중복 찜 예외를 발생시킵니다.
         */
        if (favoriteRecipeRepository.existsByUserIdAndRecipeId(user.getId(), recipe.getId())) {
            throw new CustomException(
                    FavoriteRecipeErrorCode.FAVORITE_RECIPE_ALREADY_EXISTS
            );
        }

        /*
            4. 레시피 찜 저장
            - 동시 요청으로 복합 유니크 제약을 위반하면 중복 찜 예외로 변환합니다.
         */
        FavoriteRecipe savedFavoriteRecipe;
        try {
            savedFavoriteRecipe = favoriteRecipeRepository.saveAndFlush(
                    FavoriteRecipe.create(user, recipe)
            );
        } catch (DataIntegrityViolationException exception) {
            throw new CustomException(
                    FavoriteRecipeErrorCode.FAVORITE_RECIPE_ALREADY_EXISTS
            );
        }

        log.info(
                "[FavoriteRecipeService] 레시피 찜 등록 | addFavorite() - END | favoriteRecipeId: {}",
                savedFavoriteRecipe.getId()
        );
    }

    /**
     * (1) 작업 목적
     * 사용자가 찜한 레시피를 찜 목록에서 해제합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호와 레시피 PK로 삭제 대상을 조회합니다.
     * - 사용자와 레시피 조합에 해당하는 찜 정보가 없으면 예외를 발생시킵니다.
     * - 조회된 찜 정보를 삭제합니다.
     *
     * @param recipeId 찜을 해제할 레시피 PK
     * @param userNumber 사용자 고유 식별번호
     */
    @Transactional
    public void removeFavorite(Long recipeId, Integer userNumber) {
        log.info(
                "[FavoriteRecipeService] 레시피 찜 해제 | removeFavorite() - START | recipeId: {}, userNumber: {}",
                recipeId,
                userNumber
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(String.valueOf(userNumber))
                .orElseThrow(() -> new CustomException(
                        FavoriteRecipeErrorCode.USER_NOT_FOUND
                ));

        /*
            2. 레시피 조회
            - 레시피 PK가 존재하지 않으면 RECIPE_NOT_FOUND 예외를 발생시킵니다.
         */
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new CustomException(
                        FavoriteRecipeErrorCode.RECIPE_NOT_FOUND
                ));

        /*
            3. 레시피 찜 조회
            - 사용자와 레시피 조합의 찜 정보가 없으면 FAVORITE_RECIPE_NOT_FOUND 예외를 발생시킵니다.
         */
        FavoriteRecipe favoriteRecipe = favoriteRecipeRepository.findByUserIdAndRecipeId(
                        user.getId(),
                        recipe.getId()
                )
                .orElseThrow(() -> new CustomException(
                        FavoriteRecipeErrorCode.FAVORITE_RECIPE_NOT_FOUND
                ));

        /*
            4. 레시피 찜 삭제
            - 조회된 찜 정보를 삭제합니다.
         */
        favoriteRecipeRepository.delete(favoriteRecipe);

        log.info(
                "[FavoriteRecipeService] 레시피 찜 해제 | removeFavorite() - END | favoriteRecipeId: {}",
                favoriteRecipe.getId()
        );
    }

    /**
     * (1) 작업 목적
     * 사용자가 찜한 레시피를 최신 찜순으로 조회합니다.
     *
     * (2) 세부 작업 내용
     * - 사용자 고유 식별번호로 조회 대상을 확인합니다.
     * - 위치 커서와 조회 크기로 찜 레시피 Slice를 조회합니다.
     * - 다음 위치 커서를 계산하고 찜 레시피 목록 응답으로 변환합니다.
     *
     * @param request 사용자 식별번호와 커서 조회 조건
     * @return 찜한 레시피 목록과 다음 커서 정보
     */
    @Transactional(readOnly = true)
    public FavoriteRecipeListResDto getFavoriteRecipes(
            FavoriteRecipeSearchReqDto request
    ) {
        log.info(
                "[FavoriteRecipeService] 찜한 레시피 조회 | getFavoriteRecipes() - START | userNumber: {}, cursor: {}, size: {}",
                request.userNumber(),
                request.cursor(),
                request.size()
        );

        /*
            1. 사용자 조회
            - 사용자 고유 식별번호가 존재하지 않으면 USER_NOT_FOUND 예외를 발생시킵니다.
         */
        User user = userRepository.findByLoginNumber(request.userNumber())
                .orElseThrow(() -> new CustomException(
                        FavoriteRecipeErrorCode.USER_NOT_FOUND
                ));

        /*
            2. 찜한 레시피 조회
            - 최신 찜순으로 레시피 기본 정보와 재료 통계를 위치 커서 기반 조회합니다.
         */
        Slice<RecipeSearchResult> favoriteRecipeSlice = recipeRepository.searchFavoriteRecipes(
                user.getId(),
                request.cursor(),
                request.size()
        );

        /*
            3. 찜한 레시피 응답 변환
            - 다음 데이터가 존재하면 다음 조회 위치를 계산하고 Mapper로 응답을 생성합니다.
         */
        Long nextCursor = favoriteRecipeSlice.hasNext()
                ? request.cursor() + request.size()
                : null;
        FavoriteRecipeListResDto result = favoriteRecipeMapper.toFavoriteRecipeListResDto(
                favoriteRecipeSlice,
                nextCursor
        );

        log.info(
                "[FavoriteRecipeService] 찜한 레시피 조회 | getFavoriteRecipes() - END | resultSize: {}, nextCursor: {}",
                result.content().size(),
                result.nextCursor()
        );
        return result;
    }
}
