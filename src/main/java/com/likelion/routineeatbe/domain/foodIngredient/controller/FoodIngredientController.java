package com.likelion.routineeatbe.domain.foodIngredient.controller;

import com.likelion.routineeatbe.domain.foodIngredient.dto.response.FoodIngredientResponse;
import com.likelion.routineeatbe.domain.foodIngredient.service.FoodIngredientService;
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
public class FoodIngredientController implements FoodIngredientControllerDocs{

    private final FoodIngredientService foodIngredientService;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public GlobalResponse<List<FoodIngredientResponse>> getFoodIngredients(String search){
        List<FoodIngredientResponse> foodIngredientResponseList=foodIngredientService.getFoodIngredients(search);
        return GlobalResponse.success("식재료 조회가 성공했습니다.",foodIngredientResponseList);
    }

    @Override
    public GlobalResponse<List<FoodIngredientResponse>> getAllergyFoodIngredients(){
        List<FoodIngredientResponse> foodIngredientResponseList=foodIngredientService.getAllergyFoodIngredients();
        return GlobalResponse.success("알레르기 유발 식재료 조회가 성공했습니다.",foodIngredientResponseList);
    }

    // 식재료 초기 세팅 API
    @Override
    public GlobalResponse<Integer> insertFoodIngredient(){
        // 테이블명
        String tableName = "food_ingredient";

        // 1. 실행 전 데이터 개수 조회
        Integer beforeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Integer.class);

        ResourceDatabasePopulator populator= new ResourceDatabasePopulator();

        // 한글 주석/데이터 깨짐 방지
        populator.setSqlScriptEncoding("UTF-8");

        // 실행할 sql 파일 지정 (경로에 맞게 수정)
        populator.addScript(new ClassPathResource("sql/insert_food_ingredients.sql"));

        // DB 실행
        populator.execute(dataSource);

        // 2. 실행 후 데이터 개수 조회
        Integer afterCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName, Integer.class);

        // 3. 추가된 개수 계산
        int insertedCount = (afterCount != null ? afterCount : 0) - (beforeCount != null ? beforeCount : 0);

        return GlobalResponse.success("식재료 데이터 초기화 완료",insertedCount);
    }
}
