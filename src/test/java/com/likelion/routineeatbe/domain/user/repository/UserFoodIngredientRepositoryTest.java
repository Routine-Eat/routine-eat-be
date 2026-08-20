package com.likelion.routineeatbe.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredientType;
import com.likelion.routineeatbe.domain.foodIngredient.entity.PrimaryUnit;
import com.likelion.routineeatbe.domain.foodIngredient.entity.SecondaryUnit;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredientType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:user-food-ingredient-test;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserFoodIngredientRepositoryTest {

    @Autowired
    private UserFoodIngredientRepository userFoodIngredientRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("동일 음식 재료의 OWN 주 단위 보유량을 합산하여 최대 보유 재료 조회 성공")
    void 동일_음식_재료의_OWN_주_단위_보유량을_합산하여_최대_보유_재료_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("1234").build());
        FoodIngredient potato = persistFoodIngredient("감자");
        FoodIngredient carrot = persistFoodIngredient("당근");
        FoodIngredient onion = persistFoodIngredient("양파");
        persistUserFoodIngredient(user, potato, UserFoodIngredientType.OWN, 60.0);
        persistUserFoodIngredient(user, potato, UserFoodIngredientType.OWN, 50.0);
        persistUserFoodIngredient(user, carrot, UserFoodIngredientType.OWN, 100.0);
        persistUserFoodIngredient(user, onion, UserFoodIngredientType.RESERVATION, 1000.0);
        entityManager.flush();
        entityManager.clear();

        // when
        List<FoodIngredient> result = userFoodIngredientRepository
                .findFoodIngredientsByTotalPrimaryAmountDesc(
                        user.getId(),
                        UserFoodIngredientType.OWN,
                        PageRequest.of(0, 1)
                );

        // then
        assertThat(result).extracting(FoodIngredient::getId)
                .containsExactly(potato.getId());
        assertThat(result.getFirst().getName()).isEqualTo("감자");
    }

    @Test
    @DisplayName("OWN 주 단위 보유량 합계가 같으면 음식 재료 PK 오름차순 조회 성공")
    void OWN_주_단위_보유량_합계가_같으면_음식_재료_PK_오름차순_조회_성공() {
        // given
        User user = entityManager.persist(User.builder().loginNumber("5678").build());
        FoodIngredient firstIngredient = persistFoodIngredient("첫 번째 재료");
        FoodIngredient secondIngredient = persistFoodIngredient("두 번째 재료");
        persistUserFoodIngredient(user, firstIngredient, UserFoodIngredientType.OWN, 100.0);
        persistUserFoodIngredient(user, secondIngredient, UserFoodIngredientType.OWN, 100.0);
        entityManager.flush();
        entityManager.clear();

        // when
        List<FoodIngredient> result = userFoodIngredientRepository
                .findFoodIngredientsByTotalPrimaryAmountDesc(
                        user.getId(),
                        UserFoodIngredientType.OWN,
                        PageRequest.of(0, 1)
                );

        // then
        assertThat(result).extracting(FoodIngredient::getId)
                .containsExactly(firstIngredient.getId());
    }

    private FoodIngredient persistFoodIngredient(String name) {
        return entityManager.persist(FoodIngredient.builder()
                .name(name)
                .type(FoodIngredientType.VEGETABLE)
                .pricePerHundred(1000L)
                .primaryUnit(PrimaryUnit.G)
                .secondaryUnit(SecondaryUnit.GAE)
                .exception(false)
                .build());
    }

    private void persistUserFoodIngredient(
            User user,
            FoodIngredient foodIngredient,
            UserFoodIngredientType relationType,
            Double primaryAmountValue
    ) {
        entityManager.persist(UserFoodIngredient.builder()
                .user(user)
                .foodIngredient(foodIngredient)
                .relationType(relationType)
                .primaryAmountValue(primaryAmountValue)
                .build());
    }
}
