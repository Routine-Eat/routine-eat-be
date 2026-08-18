package com.likelion.routineeatbe.domain.cookingTip.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.cookingTip.enums.CookingTipContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cooking-tip-test;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CookingTipPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("요리 팁과 콘텐츠 테이블 생성 및 저장 성공")
    void 요리_팁과_콘텐츠_저장_성공() {
        // given
        CookingTip cookingTip = CookingTip.create("대파 써는 법");
        CookingTipContent.create(
                cookingTip,
                CookingTipContentType.TEXT,
                "대파를 깨끗이 씻어 주세요.",
                1
        );

        // when
        CookingTip savedCookingTip = entityManager.persistFlushFind(cookingTip);

        // then
        assertThat(savedCookingTip.getId()).isNotNull();
        assertThat(savedCookingTip.getContents()).hasSize(1);
        assertThat(savedCookingTip.getContents().getFirst().getId()).isNotNull();
        assertThat(savedCookingTip.getContents().getFirst().getSortOrder()).isEqualTo(1);
    }
}
