-- 1. ENUM 스키마 수정
ALTER TABLE `cooking_equipment`
    MODIFY COLUMN `type` VARCHAR(255) NOT NULL;

ALTER TABLE `cooking_equipment`
    MODIFY COLUMN `type` ENUM('APPLIANCE', 'UTENSIL', 'PREP_TOOL', 'ETC') NOT NULL;

ALTER TABLE `cooking_equipment`
    MODIFY COLUMN `symbol` ENUM('ESSENTIAL', 'RECOMMEND');

START TRANSACTION;

-- 2. 임시 시드 테이블 생성 (UNION ALL 대신 안전한 구조 정의)
DROP TEMPORARY TABLE IF EXISTS `cooking_equipment_seed`;

CREATE TEMPORARY TABLE `cooking_equipment_seed` (
    `name` VARCHAR(255) NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `symbol` VARCHAR(255) NULL
);

-- 시드 데이터 삽입
INSERT INTO `cooking_equipment_seed` (`name`, `type`, `symbol`) VALUES
    -- 1. 조리기기 (APPLIANCE)
    ('전기밥솥', 'APPLIANCE', NULL),
    ('전자레인지', 'APPLIANCE', 'ESSENTIAL'),
    ('프라이팬', 'APPLIANCE', 'ESSENTIAL'),
    ('웍', 'APPLIANCE', NULL),
    ('냄비', 'APPLIANCE', 'ESSENTIAL'),
    ('뚝배기', 'APPLIANCE', NULL),
    ('찜기', 'APPLIANCE', NULL),

    -- 2. 조리도구 (UTENSIL)
    ('뒤집개', 'UTENSIL', 'RECOMMEND'),
    ('국자', 'UTENSIL', 'ESSENTIAL'),
    ('주걱', 'UTENSIL', NULL),
    ('집게', 'UTENSIL', 'RECOMMEND'),
    ('거품기', 'UTENSIL', 'RECOMMEND'),
    ('브러시', 'UTENSIL', NULL),
    ('매셔', 'UTENSIL', NULL),
    ('고기 망치', 'UTENSIL', NULL),
    ('계량스푼', 'UTENSIL', NULL),
    ('계량컵', 'UTENSIL', 'RECOMMEND'),
    ('전자저울', 'UTENSIL', 'RECOMMEND'),
    ('온도계', 'UTENSIL', NULL),
    ('믹싱볼', 'UTENSIL', NULL),
    ('채반', 'UTENSIL', 'RECOMMEND'),
    ('체', 'UTENSIL', NULL),

    -- 3. 손질도구 (PREP_TOOL)
    ('주방칼', 'PREP_TOOL', 'ESSENTIAL'),
    ('과도', 'PREP_TOOL', NULL),
    ('주방 가위', 'PREP_TOOL', 'ESSENTIAL'),
    ('감자칼', 'PREP_TOOL', NULL),
    ('도마', 'PREP_TOOL', 'ESSENTIAL'),
    ('채칼', 'PREP_TOOL', NULL),
    ('강판', 'PREP_TOOL', NULL),

    -- 4. 기타 (ETC)
    ('캔 따개', 'ETC', NULL);

-- 3. 기존 데이터가 존재할 경우 type, symbol 및 updated_at 갱신 (UPDATE)
UPDATE `cooking_equipment` AS existing
    JOIN `cooking_equipment_seed` AS source
    ON existing.`name` = source.`name`
SET
    existing.`type` = source.`type`,
    existing.`symbol` = source.`symbol`,
    existing.`updated_at` = CURRENT_TIMESTAMP;

-- 4. 존재하지 않는 신규 데이터만 추가 (INSERT)
INSERT INTO `cooking_equipment` (
    `name`,
    `type`,
    `symbol`,
    `created_at`,
    `updated_at`
)
SELECT
    source.`name`,
    source.`type`,
    source.`symbol`,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM `cooking_equipment_seed` AS source
         LEFT JOIN `cooking_equipment` AS existing
                   ON existing.`name` = source.`name`
WHERE existing.`name` IS NULL;

-- 5. 임시 테이블 정리 및 트랜잭션 종료
DROP TEMPORARY TABLE `cooking_equipment_seed`;

COMMIT;