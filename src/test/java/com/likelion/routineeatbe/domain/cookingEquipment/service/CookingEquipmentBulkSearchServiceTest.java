package com.likelion.routineeatbe.domain.cookingEquipment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.repository.CookingEquipmentRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CookingEquipmentBulkSearchServiceTest {

    @InjectMocks
    private CookingEquipmentBulkSearchService cookingEquipmentBulkSearchService;

    @Mock
    private CookingEquipmentRepository cookingEquipmentRepository;

    @Test
    @DisplayName("전체 조리 도구를 식별자 순으로 조회한다")
    void 전체_조리_도구_식별자순_조회_성공() {
        // given
        List<CookingEquipment> cookingEquipments = List.of(
                CookingEquipment.builder().id(1L).name("칼").build(),
                CookingEquipment.builder().id(2L).name("프라이팬").build()
        );
        given(cookingEquipmentRepository.findAllByOrderByTypeAscIdAsc())
                .willReturn(cookingEquipments);

        // when
        List<CookingEquipment> result = cookingEquipmentBulkSearchService.findAll();

        // then
        assertThat(result).containsExactlyElementsOf(cookingEquipments);
    }
}
