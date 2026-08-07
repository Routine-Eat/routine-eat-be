package com.likelion.routineeatbe.domain.foodIngredient.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodIngredientType {
    POTATO_AND_STARCH("감자 및 전분류"),
    NUT_AND_SEED("견과 및 종실류"),
    GRAIN("곡류"),
    FRUIT("과일류"),
    OTHER("기타"),
    EGG("난류"),
    SUGAR("당류"),
    LEGUME("콩류"),
    PROCESSED_LEGUME("콩 가공식품"),
    MUSHROOM("버섯류"),
    MILK("우유류"),
    FAT_AND_OIL("유지류"),
    MEAT("육류"),
    CONDIMENT("조미료류"),
    VEGETABLE("채소류"),
    SEAWEED("해조류"),
    FISH_AND_OTHER_SEAFOOD("어패류 및 기타 수산물"),
    SHELLFISH("조개류"),
    CRAB("게류"),
    CEPHALOPOD("두족류"),
    SEASONING("양념"),
    BASIC_SAUCE("기본 소스"),
    WESTERN_SAUCE("양식 소스"),
    ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK("중식/일식/트렌드 소스 및 간식/곡류가공"),
    NOODLE("면류"),
    RICE_CAKE("떡류"),
    BREAD_AND_WRAPPER("빵/피류"),
    JAM_AND_SPREAD("잼/스프레드류"),
    DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD("건어/알/가공"),
    KIMCHI_PICKLE_AND_FERMENTED_FOOD("김치/절임/발효식품"),
    FROZEN_PRODUCT("냉동제품"),
    DAIRY_AND_CHEESE("유제품/치즈"),
    PROCESSED_MEAT_AND_CONVENIENT_PROTEIN("육가공/간편 단백질"),
    SPICE("향신료"),
    PROCESSED_SEAFOOD("수산 가공품"),
    BAKING_AND_CONFECTIONERY_INGREDIENT("베이킹/제과 재료");

    private final String value;
}
