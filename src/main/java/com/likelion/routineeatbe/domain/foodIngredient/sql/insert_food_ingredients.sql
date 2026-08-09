-- 재료 분류.txt를 기준으로 생성한 FoodIngredient 초기 데이터
-- 동일한 이름과 타입의 데이터가 이미 존재하면 다시 삽입하지 않는다.

-- 기존 통합 수산물 타입을 신규 타입으로 안전하게 분리한다.
ALTER TABLE `food_ingredient`
    MODIFY COLUMN `type` VARCHAR(255) NOT NULL;

ALTER TABLE `food_ingredient`
    MODIFY COLUMN `type` ENUM(
        'POTATO_AND_STARCH',
        'NUT_AND_SEED',
        'GRAIN',
        'FRUIT',
        'OTHER',
        'EGG',
        'SUGAR',
        'LEGUME',
        'PROCESSED_LEGUME',
        'MUSHROOM',
        'MILK',
        'FAT_AND_OIL',
        'MEAT',
        'CONDIMENT',
        'VEGETABLE',
        'SEAWEED',
        'FISH_AND_OTHER_SEAFOOD',
        'SHELLFISH',
        'CRAB',
        'CEPHALOPOD',
        'SEASONING',
        'BASIC_SAUCE',
        'WESTERN_SAUCE',
        'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK',
        'NOODLE',
        'RICE_CAKE',
        'BREAD_AND_WRAPPER',
        'JAM_AND_SPREAD',
        'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD',
        'KIMCHI_PICKLE_AND_FERMENTED_FOOD',
        'FROZEN_PRODUCT',
        'DAIRY_AND_CHEESE',
        'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN',
        'SPICE',
        'PROCESSED_SEAFOOD',
        'BAKING_AND_CONFECTIONERY_INGREDIENT'
    ) NOT NULL;

START TRANSACTION;

-- price_per_hundred는 2026년 대한민국 소매 참고가격을 100g(G) 또는 100ml(ML)로 환산한 원화 값이다.
-- primary_unit이 G인 재료는 100g, ML인 재료는 100ml 기준이며 secondary_unit은 레시피 입력 보조 단위다.
DROP TEMPORARY TABLE IF EXISTS `food_ingredient_seed`;

CREATE TEMPORARY TABLE `food_ingredient_seed` AS
SELECT
    source.name,
    source.type,
    source.price_per_hundred,
    source.primary_unit,
    source.secondary_unit
FROM (
    SELECT '감자' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '감자전분' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고구마' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고구마 전분' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '곤약' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '단호박' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지감자' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '마' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '밤' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '쌀 전분' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '애호박' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '야콘' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '옥수수 전분' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '카사바' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '타피오카 전분' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '칡뿌리 전분' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '토란 전분' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '호박' AS name, 'POTATO_AND_STARCH' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '도토리' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '도토리묵' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '들깨' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '땅콩' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '수박씨' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '아몬드' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '은행' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '잣' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '검정깨' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '참깨' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '흰깨' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '코코넛' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '피스타치오' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '해바라기씨' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '헤이즐넛' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '호두' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '호박씨' AS name, 'NUT_AND_SEED' AS type, 2400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '귀리 (오트)' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '기장' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '단옥수수' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '메옥수수' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '찰옥수수' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '옥수수' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '콘샐러드' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '백미' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '현미' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '흑미' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '찹쌀' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '보리' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '메밀묵' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '수수' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '잡곡' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '조' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '퀴노아' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '호밀' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '통호밀' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '율무' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '강력밀가루' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '박력밀가루' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '중력밀가루' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '통밀가루' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '밀' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '흑밀' AS name, 'GRAIN' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '감' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '단감' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '대봉' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '홍시' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '곶감' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '거봉' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '샤인머스캣' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '포도' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '청포도' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '귤' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '귤(천혜향)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '귤(한라봉)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '다래' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '대추(생대추)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '대추(말린대추)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '대추야자' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '두리안' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '딸기' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '애플수박' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '수박' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '라즈베리' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '블루베리' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '산딸기' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '아로니아' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '크랜베리' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '라임' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '레몬' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '유자' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '탱자' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '리치' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '망고' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '망고스틴' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '머루' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '오디' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '오미자' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '매실' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '살구' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '앵두' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '자두' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '체리' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '버찌' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '모과' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '무화과' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '사과' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '멜론' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '참외' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '바나나' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '복숭아(백도)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '복숭아(천도)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '복숭아(황도)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '용과(백)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '용과(적)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '용과(황)' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '잭프루트' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '파파야' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '파인애플' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '자몽' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '오렌지' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '석류' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '산수유' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '아보카도' AS name, 'FRUIT' AS type, 950 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '대나무' AS name, 'OTHER' AS type, 3000 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '달팽이' AS name, 'OTHER' AS type, 3000 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '인삼(홍삼)' AS name, 'OTHER' AS type, 3000 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '달걀' AS name, 'EGG' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '메추리알' AS name, 'EGG' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '오리알' AS name, 'EGG' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '피단(송화단)' AS name, 'EGG' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '각설탕' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '백설탕' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '설탕' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '황설탕' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '흑설탕' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '빙설탕' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '꿀' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '당밀' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '분당(슈가파우더)' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '아가베 시럽' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '알룰로스' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '메이플 시럽' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '스테비아' AS name, 'SUGAR' AS type, 250 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '강낭콩' AS name, 'LEGUME' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '검은콩' AS name, 'LEGUME' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '녹두' AS name, 'LEGUME' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '대두(백태)' AS name, 'LEGUME' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '렌틸콩' AS name, 'LEGUME' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '병아리콩' AS name, 'LEGUME' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '완두' AS name, 'LEGUME' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '팥' AS name, 'LEGUME' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '건두부' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '두부' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '순두부' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '연두부' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '유부' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '두유' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '녹두묵' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '메주' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '나또' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '청국장' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '콩고기' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '콩비지' AS name, 'PROCESSED_LEGUME' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '검은비늘버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '나도팽나무버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '뽕나무버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '꾀꼬리버섯(샤방테렐)' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '꽃송이버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '영지버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '상황버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '동충하초' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '느타리버섯(애느타리)' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '느타리버섯(율무느타리)' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '능이버섯(향버섯)' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '송이버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '버들송이버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '노루궁뎅이버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '백목이버섯(은이버섯)' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '목이버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '만가닥버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '밤버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '아위버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '싸리버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '석이버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '풀버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '포타벨라' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '잎새버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '새송이버섯(큰느타리버섯)' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '양송이버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '표고버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '팽이버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '포르치니 버섯' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '트러플(송로버섯)' AS name, 'MUSHROOM' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '우유' AS name, 'MILK' AS type, 300 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '덕팻(오리기름)' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '닭기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '돼지기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '쇠기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '연어기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '면실유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '미강유(쌀겨기름)' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '팜유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '혼합식물성유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '들기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '참기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '콩기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '유채씨기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '잇꽃씨기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '포도씨유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '해바라기유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '옥수수기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '아몬드유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '아보카도유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '아마씨유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '올리브유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '코코넛유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '피스타치오유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '호두유' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '땅콩기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '복숭아씨기름' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '트러플 오일' AS name, 'FAT_AND_OIL' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '고래고기' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '닭고기' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '닭고기(간)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '닭고기(모래주머니)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '닭고기(발)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '닭고기(염통)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(갈비)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(간)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(곱창)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(등심)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(뒷다리살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(막창)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(머리고기)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(목살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(발)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(삼겹살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(수육용)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(앞다리살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(염통)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(오소리감투)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(허파)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '돼지고기(항정살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(갈비살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(간)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(곱창)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(등심살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(목심살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(사태살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(설도살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(선지)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(안심살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(앞다리살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(양지살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(염통)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(우둔살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(위)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(천엽)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(채끝살)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(허파)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소고기(혀)' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '양고기' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '오리고기' AS name, 'MEAT' AS type, 3400 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고춧가루' AS name, 'CONDIMENT' AS type, 500 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '소금' AS name, 'CONDIMENT' AS type, 500 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '후추' AS name, 'CONDIMENT' AS type, 500 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '갓' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고들빼기' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '두릅' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '머위' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '엉겅퀴' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '취나물' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '쑥' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '쑥갓' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고사리' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고구마줄기' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고추' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고추(꽈리고추)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고추(오이고추)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고추(청양고추)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고추(풋고추)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고추(홍고추)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고추냉이' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '겨자' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '산초' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '곰취' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '깻잎' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '명이나물' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '콩잎' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '냉이' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '민들레' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '방아' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '뽕잎' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '당근' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '비트' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '콜라비' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '더덕' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '도라지' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '우엉' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '연근' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '마늘' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '다진마늘' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '마늘종' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '양파' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '대파' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '쪽파' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '파' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '샬롯' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '리크' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '무' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '무순' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '무청' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '무말랭이' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '순무' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '총각무' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '열무' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '쌈무' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '단무지' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '락교' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '미나리' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '부추' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '바질' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '루꼴라' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고수' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '딜' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '파슬리' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '타임' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '월계수' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '오레가노' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '민트' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '페퍼민트' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '라벤더' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '레몬그라스(시트로넬라)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '차이브' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배추' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배추(우거지)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배추(봄동)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배추(얼갈이배추)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '양배추' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '방울양배추' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '청경채' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '케일' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '치커리' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '아욱' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '시금치' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '상추(로메인)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '샐러드채소' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '어린잎채소' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '라디키오' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '엔다이브' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '물냉이(크레스)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '브로콜리' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '콜리플라워' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '아티초크' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '박' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '죽순' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '알로에' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '사탕수수' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '사탕무' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '꾸지뽕' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '차요테' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '물밤(마름)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '샐러리' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '생강' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '숙주나물' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '콩나물' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '참나물' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '오이' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '늙은오이' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '피망' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '파프리카' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '토마토' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '방울토마토' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '가지' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '페넬(회향)' AS name, 'VEGETABLE' AS type, 750 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '가시파래(감태)' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '김' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '다시마' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '매생이' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '미역' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '바다포도' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '우뭇가사리(우무)' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '파래' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '톳' AS name, 'SEAWEED' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '가자미' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '광어(넙치)' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '도다리' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '돔' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '우럭' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '복어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '볼락' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '임연수' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '쥐치' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '고등어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '갈치' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '꽁치' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '멸치' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '밴댕이' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '청어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '방어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '부시리' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '삼치' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '전갱이' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '전어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '준치' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '대구' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '명태' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '동태' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '조기' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '굴비' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '다랑어(참치)' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '연어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '송어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '아귀' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '메기' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '뱀장어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '장어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '붕장어(아나고)' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '갯장어(하모)' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '꼼장어(먹장어)' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '숭어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '농어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '도루묵' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '꼼치' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '양미리' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '양태' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '뱅어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '빙어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '까나리' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '달고기' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '가오리' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '홍어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '상어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '멍게' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '미더덕' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '성게' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '해삼' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '군소' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '해파리' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '해면' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '갯강구' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '쏙' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '망둑어' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '베도라치' AS name, 'FISH_AND_OTHER_SEAFOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '가리비' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '굴' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '꼬막' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '명주조개' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '바지락' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '맛조개' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '새조개' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '재첩' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '전복' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '홍합' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '조개' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '다슬기' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고둥' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '골뱅이' AS name, 'SHELLFISH' AS type, 3500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '꽃게' AS name, 'CRAB' AS type, 4800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '대게' AS name, 'CRAB' AS type, 4800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '게' AS name, 'CRAB' AS type, 4800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '새우' AS name, 'CRAB' AS type, 4800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '랍스터' AS name, 'CRAB' AS type, 4800 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '낙지' AS name, 'CEPHALOPOD' AS type, 3000 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '문어' AS name, 'CEPHALOPOD' AS type, 3000 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '오징어' AS name, 'CEPHALOPOD' AS type, 3000 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '주꾸미' AS name, 'CEPHALOPOD' AS type, 3000 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '한치' AS name, 'CEPHALOPOD' AS type, 3000 AS price_per_hundred, 'G' AS primary_unit, 'MARI' AS secondary_unit
    UNION ALL SELECT '국간장' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '양조간장' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '진간장' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '반간장' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '고추장' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '된장' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '쌈장' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '까나리액젓' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '멸치액젓' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '액젓' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '참치액' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '새우젓' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '맛술' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '식초' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '매실청' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '물엿' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '올리고당' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '조청' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '멸치육수팩' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '사골육수' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '코인육수' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '조미료' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '쇠고기 조미료' AS name, 'SEASONING' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '갈릭 디핑 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '사워크림' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '나초 치즈 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '치즈 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '데리야끼소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '바비큐 소스 (BBQ 소스)' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '우스터 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '발사믹 글레이즈' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '마요네즈' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '케첩' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '머스터드' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '홀그레인 머스터드' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '허니 머스터드' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '스리라차' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '칠리소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '스위트 칠리소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '핫소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '살사 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '과카몰리' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '불닭 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '월남쌈 분짜 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '피쉬 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '카오팟 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '타르타르 소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '굴소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '돈가스소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '스테이크소스' AS name, 'BASIC_SAUCE' AS type, 600 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '레몬즙' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '랜치 드레싱' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '바질페스토' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '발사믹식초' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '시저 드레싱' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '오리엔탈 드레싱' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '샐러드드레싱' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '천섬(싸우전드 아일랜드) 드레싱' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '치미추리 소스' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '토마토소스' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '크림소스' AS name, 'WESTERN_SAUCE' AS type, 850 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '간장소스' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '쯔유' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '미소된장' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '폰즈 소스' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '고추기름' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '두반장' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '마라소스' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '춘장' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '해선장(호이신 소스)' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT 'XO 소스' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '삼발 소스' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '팟타이 소스' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '카레가루' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '밥' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '오트밀' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '시리얼' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '그래놀라' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '아몬드버터' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '타피오카 펄' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '트러플 페이스트' AS name, 'ASIAN_TREND_SAUCE_AND_PROCESSED_GRAIN_SNACK' AS type, 900 AS price_per_hundred, 'ML' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '당면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '중국당면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '납작당면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '분모자' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '라면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '냉면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '막국수면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '메밀면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '소바' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '옥수수면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '쌀국수면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '수제비' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '뇨키' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '우동면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '소면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '중면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '칼국수면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '쫄면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '중화면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '파스타면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '스파게티면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '마카로니' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '라자냐면' AS name, 'NOODLE' AS type, 450 AS price_per_hundred, 'G' AS primary_unit, 'INBUN' AS secondary_unit
    UNION ALL SELECT '가래떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '떡국떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '누들떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '밀떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '쌀떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '조랭이떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '절편' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '치즈떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '감자떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '옥수수떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '백설기' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '찹쌀떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '구슬떡' AS name, 'RICE_CAKE' AS type, 550 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '만두피' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '라이스페이퍼' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '또띠아' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '타코쉘' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '춘권피' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '크레페 피' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '퍼프 페이스트리 시트(파이피)' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '식빵' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '통밀빵' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '모닝빵' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '베이글' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '치아바타' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '바게트' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '호밀빵' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '포카치아' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '피타빵' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '잉글리시 머핀' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '사워도우' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '피자도우' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '빵가루' AS name, 'BREAD_AND_WRAPPER' AS type, 900 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '딸기잼' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '블루베리잼' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '라즈베리잼' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '사과잼' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '무화과잼' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '오렌지 마멀레이드' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '카야잼' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '땅콩버터' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '누텔라' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '로투스 스프레드' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '녹차 크림 스프레드' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '말차 크림 스프레드' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '크림치즈' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '리코타 치즈' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '에그마요' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '참치마요' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '와사비마요' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '갈릭마요' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '스리라차마요' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '딜마요' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '홀스래디시 소스' AS name, 'JAM_AND_SPREAD' AS type, 1300 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '고등어통조림' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '꽁치통조림' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '골뱅이통조림' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '번데기통조림' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '연어통조림' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '참치캔' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '날치알' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '명란' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '우니(성게알)' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '캐비아' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '맛살' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '어묵' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '멸치' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '국물용 멸치' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '북어' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '북어채' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '황태' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '황태채' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '진미채' AS name, 'DRIED_SEAFOOD_ROE_AND_PROCESSED_FOOD' AS type, 2800 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '깻잎김치' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '깍두기' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '동치미' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '총각김치' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '열무김치' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배초향김치' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배추김치' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배추김치(신김치)' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '배추김치(묵은지)' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '부추김치' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '오이김치' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '파김치' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '단무지' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '피클' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '락교' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '장아찌' AS name, 'KIMCHI_PICKLE_AND_FERMENTED_FOOD' AS type, 650 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '냉동다진마늘' AS name, 'FROZEN_PRODUCT' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '냉동만두' AS name, 'FROZEN_PRODUCT' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '냉동새우' AS name, 'FROZEN_PRODUCT' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '냉동채소' AS name, 'FROZEN_PRODUCT' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '옥수수통조림' AS name, 'FROZEN_PRODUCT' AS type, 1100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '기 (Ghee) 버터' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '무염버터' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '버터' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '생크림' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '휘핑크림' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '연유' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '무당연유(에바포레이트 밀크)' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '플레인 요거트' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '그릭요거트' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '모차렐라치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '부라타치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '슬라이스 치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '체다치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '크림치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '파마산치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '페타치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '리코타치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '브리 치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '카망베르 치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '마스카포네' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '산양유 치즈(고트 치즈)' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '하바티 치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '에멘탈 치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고다 치즈' AS name, 'DAIRY_AND_CHEESE' AS type, 1600 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '관찰레' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '하몽' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '프로슈토' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '살라미' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '페퍼로니' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '초리조' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '판체타' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '닭가슴살' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '훈제 닭가슴살' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '대패삼겹살' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '차돌박이' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '샤브샤브용 소고기' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '다진 소고기' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '다진 돼지고기' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '풀드포크' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '미트볼' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '베이컨' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '소시지' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '비엔나소시지' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '햄' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '통조림 햄(스팸 등)' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '런천미트' AS name, 'PROCESSED_MEAT_AND_CONVENIENT_PROTEIN' AS type, 2100 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '강황(터머릭)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '큐민(츠란)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '팔각(팔각향)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '정향' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '산초(마라향신료)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '넛맥(육두구)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '카다멈' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '코리앤더 씨드(고수 씨)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '펜넬 씨드' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '페누그릭' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '메이스' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '계피' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '시나몬가루' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '건고추' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '페페론치노' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '케이프런(파프리카 가루)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '바질(드라이 바질)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '오레가노' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '파슬리 (파슬리가루)' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '타임' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '로즈마리' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '월계수 잎' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '케이퍼' AS name, 'SPICE' AS type, 1800 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '앤초비(엔초비 페이스트 포함)' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '고등어통조림' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '꽁치통조림' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '연어통조림' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '골뱅이통조림' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '번데기통조림' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '참치캔' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '국물용 멸치' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '멸치' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '북어채' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '황태채' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '명란젓' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '새우젓' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '크래미' AS name, 'PROCESSED_SEAFOOD' AS type, 2500 AS price_per_hundred, 'G' AS primary_unit, 'GAE' AS secondary_unit
    UNION ALL SELECT '바닐라빈' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '바닐라익스트랙' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '바닐라오일' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '베이킹소다' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '베이킹파우더' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '이스트(드라이이스트)' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '슈가파우더' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '아몬드가루' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '타르타르크림 (크림오브타타)' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '젤라틴(판젤라틴/가루젤라틴)' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '한천가루' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '초콜릿(커버춰 초콜릿)' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '초코칩' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
    UNION ALL SELECT '코코아가루' AS name, 'BAKING_AND_CONFECTIONERY_INGREDIENT' AS type, 1200 AS price_per_hundred, 'G' AS primary_unit, 'TSP' AS secondary_unit
) AS source;

UPDATE `food_ingredient` AS existing
JOIN `food_ingredient_seed` AS source
    ON existing.`name` = source.name
    AND existing.`type` = source.type
SET
    existing.`price_per_hundred` = source.price_per_hundred,
    existing.`primary_unit` = source.primary_unit,
    existing.`secondary_unit` = source.secondary_unit,
    existing.`updated_at` = CURRENT_TIMESTAMP;

INSERT INTO `food_ingredient` (
    `name`,
    `type`,
    `price_per_hundred`,
    `primary_unit`,
    `secondary_unit`,
    `created_at`,
    `updated_at`
)
SELECT
    source.name,
    source.type,
    source.price_per_hundred,
    source.primary_unit,
    source.secondary_unit,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM `food_ingredient_seed` AS source
LEFT JOIN `food_ingredient` AS existing
    ON existing.`name` = source.name
    AND existing.`type` = source.type
WHERE existing.`id` IS NULL;

DROP TEMPORARY TABLE `food_ingredient_seed`;

COMMIT;
