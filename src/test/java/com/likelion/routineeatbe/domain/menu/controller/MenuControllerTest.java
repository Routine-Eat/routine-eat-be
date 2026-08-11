package com.likelion.routineeatbe.domain.menu.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeCookingEquipmentResDto;
import com.likelion.routineeatbe.domain.menu.dto.response.InitMenuAndRecipeFoodIngredientResDto;
import com.likelion.routineeatbe.domain.menu.service.InitMenuAndRecipeCookingEquipmentService;
import com.likelion.routineeatbe.domain.menu.service.InitMenuAndRecipeFoodIngredientService;
import com.likelion.routineeatbe.domain.menu.service.MenuAndRecipeCrawlingService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @InjectMocks
    private MenuController menuController;

    @Mock
    private MenuAndRecipeCrawlingService menuAndRecipeCrawlingService;

    @Mock
    private InitMenuAndRecipeFoodIngredientService initService;

    @Mock
    private InitMenuAndRecipeCookingEquipmentService initCookingEquipmentService;

    @Test
    @DisplayName("메뉴 음식 재료 초기화 API가 201과 초기화 개수를 반환한다")
    void 메뉴_음식_재료_초기화_API_201_반환_성공() {
        // given
        given(initService.initialize()).willReturn(InitMenuAndRecipeFoodIngredientResDto.create(1100L));

        // when
        ResponseEntity<GlobalResponse<InitMenuAndRecipeFoodIngredientResDto>> response =
                menuController.initMenuAndRecipeFoodIngredients();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getCode()).isEqualTo(201);
        assertThat(response.getBody().getMessage())
                .isEqualTo("메뉴/레시피별 필요 음식 재료 데이터 초기화를 성공했습니다.");
        assertThat(response.getBody().getData().initCount()).isEqualTo(1100L);
    }

    @Test
    @DisplayName("레시피 조리 도구 초기화 API가 201과 초기화 개수를 반환한다")
    void 레시피_조리_도구_초기화_API_201_반환_성공() {
        // given
        given(initCookingEquipmentService.initialize())
                .willReturn(InitMenuAndRecipeCookingEquipmentResDto.create(1100L));

        // when
        ResponseEntity<GlobalResponse<InitMenuAndRecipeCookingEquipmentResDto>> response =
                menuController.initMenuAndRecipeCookingEquipments();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getCode()).isEqualTo(201);
        assertThat(response.getBody().getMessage())
                .isEqualTo("메뉴/레시피별 필요 조리 도구 데이터 초기화를 성공했습니다.");
        assertThat(response.getBody().getData().initCount()).isEqualTo(1100L);
    }
}
