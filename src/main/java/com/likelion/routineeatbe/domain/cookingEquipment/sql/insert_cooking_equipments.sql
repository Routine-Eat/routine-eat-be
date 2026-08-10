-- 조리도구 분류를 기준으로 생성한 CookingEquipment 초기 데이터
-- 동일한 이름의 데이터가 이미 존재하면 타입을 갱신한다.

-- 신규 타입을 안전하게 적용하기 위한 ENUM 스키마 수정
ALTER TABLE `cooking_equipment`
    MODIFY COLUMN `type` VARCHAR(255) NOT NULL;

ALTER TABLE `cooking_equipment`
    MODIFY COLUMN `type` ENUM(
    'APPLIANCE',
    'UTENSIL',
    'PREP_TOOL',
    'ETC'
    ) NOT NULL;

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS `cooking_equipment_seed`;

CREATE TEMPORARY TABLE `cooking_equipment_seed` AS
SELECT
    source.name,
    source.type
FROM (
         -- 1. 조리기기 (APPLIANCE)
         SELECT '전기밥솥' AS name, 'APPLIANCE' AS type
         UNION ALL SELECT '전자레인지', 'APPLIANCE'
         UNION ALL SELECT '프라이팬', 'APPLIANCE'
         UNION ALL SELECT '웍', 'APPLIANCE'
         UNION ALL SELECT '냄비', 'APPLIANCE'
         UNION ALL SELECT '뚝배기', 'APPLIANCE'
         UNION ALL SELECT '찜기', 'APPLIANCE'

         -- 2. 조리도구 (UTENSIL)
         UNION ALL SELECT '뒤집개', 'UTENSIL'
         UNION ALL SELECT '국자', 'UTENSIL'
         UNION ALL SELECT '주걱', 'UTENSIL'
         UNION ALL SELECT '집게', 'UTENSIL'
         UNION ALL SELECT '거품기', 'UTENSIL'
         UNION ALL SELECT '튀김 건지개 / 망국자', 'UTENSIL'
         UNION ALL SELECT '브러시', 'UTENSIL'
         UNION ALL SELECT '매셔', 'UTENSIL'
         UNION ALL SELECT '고기 망치', 'UTENSIL'
         UNION ALL SELECT '계량스푼', 'UTENSIL'
         UNION ALL SELECT '계량컵', 'UTENSIL'
         UNION ALL SELECT '전자저울', 'UTENSIL'
         UNION ALL SELECT '온도계', 'UTENSIL'
         UNION ALL SELECT '믹싱볼', 'UTENSIL'
         UNION ALL SELECT '채반', 'UTENSIL'
         UNION ALL SELECT '체', 'UTENSIL'

         -- 3. 손질도구 (PREP_TOOL)
         UNION ALL SELECT '주방칼', 'PREP_TOOL'
         UNION ALL SELECT '과도', 'PREP_TOOL'
         UNION ALL SELECT '주방 가위', 'PREP_TOOL'
         UNION ALL SELECT '감자칼', 'PREP_TOOL'
         UNION ALL SELECT '도마', 'PREP_TOOL'
         UNION ALL SELECT '채칼', 'PREP_TOOL'
         UNION ALL SELECT '강판', 'PREP_TOOL'

         -- 4. 기타 (ETC)
         UNION ALL SELECT '캔 따개', 'ETC'
     ) AS source;

-- 기존 데이터가 존재할 경우 type 및 updated_at 갱신
UPDATE `cooking_equipment` AS existing
    JOIN `cooking_equipment_seed` AS source
ON existing.`name` = source.name
    SET
        existing.`type` = source.type,
        existing.`updated_at` = CURRENT_TIMESTAMP;

-- 신규 데이터 삽입
INSERT INTO `cooking_equipment` (
    `name`,
    `type`,
    `created_at`,
    `updated_at`
)
SELECT
    source.name,
    source.type,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM `cooking_equipment_seed` AS source
         LEFT JOIN `cooking_equipment` AS existing
                   ON existing.`name` = source.name
WHERE existing.`name` IS NULL;

DROP TEMPORARY TABLE `cooking_equipment_seed`;

COMMIT;