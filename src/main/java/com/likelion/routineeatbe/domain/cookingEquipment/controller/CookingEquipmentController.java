package com.likelion.routineeatbe.domain.cookingEquipment.controller;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipmentSymbol;
import com.likelion.routineeatbe.domain.cookingEquipment.service.CookingEquipmentService;
import com.likelion.routineeatbe.global.response.GlobalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CookingEquipmentController implements CookingEquipmentControllerDocs {
    private final CookingEquipmentService cookingEquipmentService;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public GlobalResponse<List<CookingEquipmentResponse>> getCookingEquipments(String search, CookingEquipmentSymbol symbol){
        List<CookingEquipmentResponse> cookingEquipmentResponseList = cookingEquipmentService.getCookingEquipments(search,symbol);
        return GlobalResponse.success(200,"조리도구 조회가 성공했습니다.",cookingEquipmentResponseList);
    }

    @Override
    public GlobalResponse<Integer> insertCookingEquipment(){
        // 테이블명
        String tableName = "cooking_equipment";

        // 1. 실행 전 데이터 개수 조회
        Integer beforeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Integer.class);

        ResourceDatabasePopulator populator= new ResourceDatabasePopulator();

        // 한글 주석/데이터 깨짐 방지
        populator.setSqlScriptEncoding("UTF-8");

        // 실행할 sql 파일 지정 (경로에 맞게 수정)
        populator.addScript(new ClassPathResource("sql/insert_cooking_equipments.sql"));

        // DB 실행
        populator.execute(dataSource);

        // 2. 실행 후 데이터 개수 조회
        Integer afterCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Integer.class);

        // 3. 추가된 개수 계산
        int insertedCount = (afterCount != null ? afterCount : 0) - (beforeCount != null ? beforeCount : 0);

        return GlobalResponse.success(201,"조리도구 데이터 초기화 완료",insertedCount);
    }
}
