package com.likelion.routineeatbe.domain.user.service;

import com.likelion.routineeatbe.domain.cookingEquipment.dto.response.CookingEquipmentResponse;
import com.likelion.routineeatbe.domain.cookingEquipment.entity.CookingEquipment;
import com.likelion.routineeatbe.domain.cookingEquipment.repository.CookingEquipmentRepository;
import com.likelion.routineeatbe.domain.foodIngredient.entity.FoodIngredient;
import com.likelion.routineeatbe.domain.foodIngredient.repository.FoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.dto.request.CreateUserFoodIngredientRequest;
import com.likelion.routineeatbe.domain.user.dto.request.UserOnboardingRequest;
import com.likelion.routineeatbe.domain.user.dto.response.UserFoodIngredientResponse;
import com.likelion.routineeatbe.domain.user.dto.response.UserOnboardingResponse;
import com.likelion.routineeatbe.domain.user.dto.response.UserResponse;
import com.likelion.routineeatbe.domain.user.entity.User;
import com.likelion.routineeatbe.domain.user.entity.UserCookingEquipment;
import com.likelion.routineeatbe.domain.user.entity.UserFoodIngredient;
import com.likelion.routineeatbe.domain.user.exception.UserCookingEquipmentErrorCode;
import com.likelion.routineeatbe.domain.user.exception.UserFoodIngredientErrorCode;
import com.likelion.routineeatbe.domain.user.repository.UserCookingEquipmentRepository;
import com.likelion.routineeatbe.domain.user.repository.UserFoodIngredientRepository;
import com.likelion.routineeatbe.domain.user.repository.UserRepository;
import com.likelion.routineeatbe.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 사용자 온보딩(초기 회원 설정) 데이터를 일괄 처리하는 서비스 클래스.
 * 요리 실력, 선호/비선호/알레르기 식재료, 보유 조리도구 정보를 단일 트랜잭션 내에서 통합 저장합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor // final이 붙은 필드들의 생성자를 자동 생성하여 스프링 의존성 주입(DI) 처리
public class UserOnboardingService {

    // 도메인별 데이터를 CRUD하기 위한 레포지토리 의존성 주입
    private final UserRepository userRepository;
    private final FoodIngredientRepository foodIngredientRepository;
    private final UserFoodIngredientRepository userFoodIngredientRepository;
    private final CookingEquipmentRepository cookingEquipmentRepository;
    private final UserCookingEquipmentRepository userCookingEquipmentRepository;

    /**
     * 사용자 온보딩(초기 회원가입 데이터) 통합 저장 메서드.
     * 단일 트랜잭션 범위 내에서 유저 스킬 레벨, 식재료 정보(알레르기, 비선호, 보유), 조리도구를 일괄 처리합니다.
     *
     * @param userId 온보딩을 진행할 대상 사용자 ID
     * @param request 사용자 온보딩 요청 데이터 DTO
     * @return 온보딩 처리 결과가 조립된 UserOnboardingResponse DTO
     */
    @Transactional // 메소드 내 모든 DB 작업의 원자성(Atomicity)을 보장. 도중에 예외 발생 시 전체 롤백
    public UserOnboardingResponse saveOnboardingData(Long userId, UserOnboardingRequest request) {
        // 1. 유저 데이터 조회
        // ID로 DB를 조회하며, 유저가 존재하지 않을 경우 프로젝트 공통 커스텀 예외(NOT_EXIST_USER)를 즉시 발생시킴
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_USER));

        // 2. 요리 실력(SkillLevel) 업데이트
        // request.skillLevel()이 null이 아닌 경우에만 수정 로직 호출
        // JPA 변경 감지(Dirty Checking) 덕분에 user.updateSkillLevel() 호출 후 repository.save()를 명시하지 않아도 트랜잭션 종료 시 UPDATE 쿼리 실행
        if (request.skillLevel() != null) {
            user.updateSkillLevel(request.skillLevel());
        }

        // 3. 식재료 정보(알레르기, 비선호, 보유) 개별 처리
        // DTO 변경에 따라 단일 CreateUserFoodIngredientRequest 객체를 직접 헬퍼 메서드로 전달
        // 각 호출별로 해당 타입(ALLERGY, DISLIKE, OWN)에 맞는 DB 저장 및 Response DTO를 각각 생성받음
        UserFoodIngredientResponse exceptionResponse = processUserFoodIngredients(user, request.exceptionIngredientList());
        UserFoodIngredientResponse ownResponse = processUserFoodIngredients(user, request.ownIngredientList());

        // 4. 조리도구 정보 일괄 처리
        // 사용자가 소유한 조리도구 ID 리스트를 기반으로 매핑 엔티티 생성 및 DB 저장
        List<CookingEquipmentResponse> equipmentResponses = processUserCookingEquipments(user, request.cookingEquipmentIdList());

        // 5. 최종 응답 객체 조립 및 반환
        // 생성된 모든 하위 Response DTO들을 하나로 합성하여 클라이언트에 최종 응답
        return UserOnboardingResponse.from(
                UserResponse.fromUserEntity(user),
                exceptionResponse,
                ownResponse,
                equipmentResponses
        );
    }

    /**
     * 단일 CreateUserFoodIngredientRequest 요청 객체를 받아 식재료 연관 관계 데이터를 처리하는 헬퍼 메서드.
     * N+1 문제 방지를 위해 단일 IN 쿼리를 사용하며, 수량 정보 및 연관 관계를 매핑하여 일괄 저장합니다.
     *
     * @param user 연관 관계를 맺을 대상 User 엔티티
     * @param request 단일 카테고리(알레르기/비선호/보유 중 1개)의 식재료 설정 요청 DTO
     * @return 처리된 결과를 담은 UserFoodIngredientResponse (요청 데이터가 없는 경우 null 반환)
     */
    private UserFoodIngredientResponse processUserFoodIngredients(User user, CreateUserFoodIngredientRequest request) {
        // [가드 클라우스] 요청 객체 자체가 null이거나, 내부 식재료 리스트가 비어있다면 불필요한 DB 조회 없이 즉시 null 리턴
        if (request == null || request.foodIngredientList() == null || request.foodIngredientList().isEmpty()) {
            return null;
        }

        // 1. 요청된 DTO 리스트에서 식재료 ID(foodIngredientId)만 추출
        // .distinct()를 통해 혹시 모를 중복 ID 입력 요청을 제거하여 조회 쿼리 최적화
        List<Long> requestedIds = request.foodIngredientList().stream()
                .map(CreateUserFoodIngredientRequest.FoodIngredientDto::foodIngredientId)
                .distinct()
                .toList();

        // 2. [최적화 - IN 쿼리 배치 조회]
        // 요청된 모든 식재료 엔티티를 단 1번의 SELECT ... WHERE id IN (...) 쿼리로 일괄 조회
        List<FoodIngredient> foodIngredients = foodIngredientRepository.findAllById(requestedIds);

        // 3. [유효성 검증]
        // 요청한 유일한 ID 개수와 DB에서 실제 찾아낸 엔티티 개수가 다르면 존재하지 않는 식재료 ID가 포함된 것임
        if (foodIngredients.size() != requestedIds.size()) {
            throw new CustomException(UserFoodIngredientErrorCode.NOT_EXIST_FOODINGREDIENT);
        }

        // 4. [LOOKUP 최적화 - List -> Map 변환]
        // 이후 반복문 매핑 과정에서 식재료를 빠르게 찾을 수 있도록 Map<ID, FoodIngredient> 형태로 변환 (탐색 시간복잡도 O(1) 보장)
        Map<Long, FoodIngredient> foodIngredientMap = foodIngredients.stream()
                .collect(Collectors.toMap(FoodIngredient::getId, Function.identity()));

        // 5. UserFoodIngredient (매핑 엔티티) 생성
        // DTO의 각 식재료 정보와 수량(primaryAmountValue, secondaryAmountValue)을 꺼내 매핑 엔티티 생성
        List<UserFoodIngredient> userFoodIngredientsToSave = request.foodIngredientList().stream()
                .map(dto -> UserFoodIngredient.createUserFoodIngredient(
                        user,                                          // FK 1: 연관 유저 엔티티
                        foodIngredientMap.get(dto.foodIngredientId()), // FK 2: Map에서 O(1)로 가져온 식재료 엔티티
                        request.relationType(),                        // 관계 타입 (OWN, DISLIKE, ALLERGY 등)
                        dto.primaryAmountValue(),                      // 기본 단위 수량
                        dto.secondaryAmountValue()                     // 보조 단위 수량
                ))
                .toList();

        // 6. DB 일괄 저장
        // JPA saveAll()을 호출하여 생성된 매핑 엔티티 목록을 한 번에 저장 (Batch Insert)
        List<UserFoodIngredient> savedEntities = userFoodIngredientRepository.saveAll(userFoodIngredientsToSave);

        // 7. 결과 응답 DTO 생성 후 리턴
        return UserFoodIngredientResponse.of(request.relationType(), savedEntities);
    }

    /**
     * 사용자가 선택한 조리도구 ID 목록을 검증한 뒤, 유저-조리도구 매핑 테이블에 일괄 저장합니다.
     *
     * @param user 연관 관계를 맺을 대상 User 엔티티
     * @param equipmentIdList 저장하려는 조리도구 Primary Key(ID) 리스트
     * @return 저장된 조리도구 응답 DTO 목록
     */
    private List<CookingEquipmentResponse> processUserCookingEquipments(User user, List<Long> equipmentIdList) {
        // [방어적 코드] 입력 리스트가 없는 경우 빈 리스트를 반환하여 NullPointerException 방지
        if (equipmentIdList == null || equipmentIdList.isEmpty()) {
            throw new CustomException(UserCookingEquipmentErrorCode.BLANK_LIST);
        }

        // [일괄 조회] 조리도구 Master 테이블에서 ID 목록에 해당하는 데이터를 IN 쿼리로 한 번에 조회
        List<CookingEquipment> cookingEquipments = cookingEquipmentRepository.findAllById(equipmentIdList);

        // [정상 검증] 요청 개수와 DB 조회 개수를 비교하여 유효하지 않은 조리도구 ID가 섞여있으면 에러 발생
        if (cookingEquipments.size() != equipmentIdList.size()) {
            throw new CustomException(UserCookingEquipmentErrorCode.NOT_EXIST_COOKINGEQUIPMENT);
        }

        // UserCookingEquipment 매핑 엔티티 객체 생성
        List<UserCookingEquipment> userCookingEquipments = cookingEquipments.stream()
                .map(cookingEquipment -> UserCookingEquipment.createUserCookingEquipment(user, cookingEquipment))
                .toList();

        // DB 일괄 저장 (Batch Insert)
        userCookingEquipmentRepository.saveAll(userCookingEquipments);

        // 엔티티를 응답 DTO인 CookingEquipmentResponse로 변환하여 리스트로 반환
        return cookingEquipments.stream()
                .map(CookingEquipmentResponse::from)
                .toList();
    }
}