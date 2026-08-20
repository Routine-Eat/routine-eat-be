package com.likelion.routineeatbe.domain.cookingTip.repository;

import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CookingTipInitializationRepository {

    private static final String COOKING_TIP_SQL_PATH = "sql/insert_cooking_tips.sql";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 요리 팁 SQL 시드 파일을 DB에 실행합니다.
     * - UTF-8 인코딩으로 SQL 파일을 읽어 한글 데이터가 깨지지 않도록 합니다.
     * - SQL 파일 내부 트랜잭션과 동기화 로직을 순서대로 실행합니다.
     */
    public void initialize() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());
        populator.addScript(new ClassPathResource(COOKING_TIP_SQL_PATH));
        populator.execute(dataSource);
    }

    /**
     * DB에 저장된 전체 요리 팁 개수를 조회합니다.
     *
     * @return 전체 요리 팁 개수
     */
    public long countCookingTips() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cooking_tip",
                Long.class
        );
        return count == null ? 0L : count;
    }

    /**
     * DB에 저장된 전체 요리 팁 콘텐츠 개수를 조회합니다.
     *
     * @return 전체 요리 팁 콘텐츠 개수
     */
    public long countCookingTipContents() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cooking_tip_content",
                Long.class
        );
        return count == null ? 0L : count;
    }
}
