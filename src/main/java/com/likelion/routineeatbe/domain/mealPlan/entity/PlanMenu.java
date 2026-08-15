package com.likelion.routineeatbe.domain.mealPlan.entity;

import com.likelion.routineeatbe.domain.mealPlan.dto.request.CreatePlanMenuRequest;
import com.likelion.routineeatbe.domain.menu.entity.Menu;
import com.likelion.routineeatbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "plan_menu")
public class PlanMenu extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(comment = "메뉴 완료 날짜")
    private LocalDate date;

    @Column(nullable = false,comment = "메뉴 완료 여부")
    private Boolean completed=false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    public static PlanMenu createPlanMenu(CreatePlanMenuRequest request){
        return PlanMenu.builder()
                .mealPlan(request.mealPlan())
                .menu(request.menu())
                .completed(false)
                .build();
    }
    public void updatePlanMenuCompleted(Boolean completed){
        this.completed=completed;
        if (Boolean.TRUE.equals(completed)) {
            this.date = LocalDate.now();
        } else {
            this.date = null;
        }
    }
}
