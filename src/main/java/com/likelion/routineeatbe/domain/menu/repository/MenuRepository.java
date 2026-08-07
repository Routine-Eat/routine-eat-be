package com.likelion.routineeatbe.domain.menu.repository;

import com.likelion.routineeatbe.domain.menu.entity.Menu;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * 입력받은 메뉴명 중 DB에 이미 존재하는 메뉴명을 조회합니다.
     *
     * @param names 존재 여부를 확인할 메뉴명 목록
     * @return DB에 존재하는 메뉴명 집합
     */
    @Query("select menu.name from Menu menu where menu.name in :names")
    Set<String> findExistingNames(@Param("names") Collection<String> names);
}
