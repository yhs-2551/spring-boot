package com.yhs.blog.springboot.jpa.domain.category.service.impl;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.cache.CacheConstants;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequest;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryWithChildrenResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.domain.category.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserFindService userFindService;
    private final RedisTemplate<String, List<CategoryWithChildrenResponse>> categoryResponseRedisTemplate;
    // 전역 멤버 변수를 사용하면 동시성 문제가 있을 수 있기 때문에, 메모리를 조금 더 사용하지만 안전하게 ThreadLocal 사용
    private final ThreadLocal<List<Category>> toBeSavedAllCategories = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<String>> toBeDeletedAllCategories = ThreadLocal.withInitial(ArrayList::new);

    @Override
    @Transactional
    public void createCategory(CategoryRequestPayLoad categoryRequestPayLoad, BlogUser blogUser) {

        log.info("[CategoryServiceImpl] createCategory 메서드 시작");

        // 카테고리 삭제 작업
        if (categoryRequestPayLoad.getCategoryToDelete() != null
                && !categoryRequestPayLoad.getCategoryToDelete().isEmpty()) {

            log.info("[CategoryServiceImpl] createCategory 메서드 - 삭제할 카테고리가 있을 경우 삭제할 카테고리 분기 진행");

            categoryRequestPayLoad.getCategoryToDelete().forEach(this::deleteCategory);
            List<String> deletedCategories = toBeDeletedAllCategories.get();
            categoryRepository.deleteAllByCategoryId(deletedCategories);
            toBeDeletedAllCategories.remove();

            // 삭제된 엔티티들 즉 영속성 컨텍스트 변경 사항을 즉시 DB에 반영. 이게 없으면, 프론트에서 부모에서 자식 카테고리를 없애고 해당 부모
            // 카테고리를 제거할 때 duplicate key 에러 발생
            // 만약 동일한 트랜잭션 내에서 이후에 작업이 실패하면 트랜잭션 완료가 아니기 때문에 flush로 작업한 내용도 롤백됨. 트랜잭션의 원자성
            // 보장
            // categoryRepository.flush();
        }

        if (categoryRequestPayLoad.getCategories() == null || categoryRequestPayLoad.getCategories().isEmpty()) {

            log.info("[CategoryServiceImpl] createCategory 메서드 - 새롭게 생성 및 수정할 카테고리가 없는 경우 분기 진행");

            return;
        }

        // 그냥 userId는 메서드 파라미터를 통해 넘기기. 전역 필드를 사용하면 싱글톤에서 공유, 동시성 이슈 등 여러 문제가 발생할 수 있음
        Long userId = blogUser.getUserIdFromToken();

        // List<Category> savedCategories = new ArrayList<>();
        long orderIndex = 0L; // 프론트측에서 요청이 오는 순서대로 orderIndex 값을 설정한다.
        // 최종적으로 프론트에서 설정한 카테고리 구조를 그대로 화면에 보여주기 위함.
        for (CategoryRequest categoryRequest : categoryRequestPayLoad.getCategories()) {

            // savedCategories.add(saveCategoryHierarchy(categoryRequest, orderIndex,
            // userId));
            toBeSavedAllCategories.get().add(saveCategoryHierarchy(categoryRequest, orderIndex, userId));
            orderIndex++;
        }

        List<Category> categories = toBeSavedAllCategories.get();
        categoryRepository.saveAll(categories);
        toBeSavedAllCategories.remove();

        // categoryRepository.saveAll(savedCategories);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 카테고리 작업 시 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화
                        categoryResponseRedisTemplate.delete("categories:" + blogUser.getBlogIdFromToken());
                    }

                });

    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryWithChildrenResponse> getAllCategoriesWithChildrenByUserId(String blogId) {

        log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 시작");

        String key = "categories:" + blogId;

        List<CategoryWithChildrenResponse> redisCategories = categoryResponseRedisTemplate.opsForValue().get(key);

        if (redisCategories != null && !redisCategories.isEmpty()) {

            log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 Redis 캐시에 존재하는 경우 분기 시작");

            return redisCategories;
        }

        log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 Redis 캐시에 존재하지 않는 경우 분기 시작");

        Long userId = userFindService.findUserByBlogId(blogId).getId();

        List<Category> CategoriesByUserId = categoryRepository.findByUserId(userId);

        if (CategoriesByUserId == null || CategoriesByUserId.isEmpty()) {

            log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 DB에 카테고리가 존재하지 않는 경우 분기 진행");

            return Collections.emptyList();
        }

        // 특정 사용자에 카테고리가 존재하는 경우에만 상세 쿼리 실행
        List<CategoryWithChildrenResponse> responseCategories = categoryRepository
                .findAllWithChildrenAndPostsByUserId(userId);

        log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 DB에 카테고리가 존재하는 경우 분기 진행");

        categoryResponseRedisTemplate.opsForValue().set(key, responseCategories, CacheConstants.CATEGORY_CACHE_HOURS,
                TimeUnit.HOURS);

        return responseCategories;

    }

    @Loggable
    @Override
    public Category findCategoryByNameAndUserId(String categoryName, Long userId) {

        log.info("[CategoryServiceImpl] findCategoryByNameAndUserId 메서드 시작");

        Category category = categoryRepository.findByNameAndUserId(categoryName, userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.CATEGORY_NOT_FOUND,
                        categoryName + " 카테고리를 찾을 수 없습니다.",
                        "CategoryServiceImpl",
                        "findCategoryByNameAndUserId"));

        return category;

    }

    // 아예 새로운 카테고리는 백엔드에 삭제 요청 안오도록 프론트에서 isNew로 구분해놨음
    @Loggable
    private void deleteCategory(CategoryRequest categoryRequest) {

        log.info("[CategoryServiceImpl] deleteCategory 메서드 시작");

        if (categoryRequest.getChildren() != null && !categoryRequest.getChildren().isEmpty()) {

            throw new BusinessException(
                    ErrorCode.CATEGORY_HAS_CHILDREN,
                    "카테고리 UUID: " + categoryRequest.getCategoryUuid() + "는 자식 카테고리가 존재하여 삭제할 수 없습니다.",
                    "CategoryServiceImpl",
                    "deleteCategory");

        }

        if (categoryRequest.getPostCount() > 0) {

            throw new BusinessException(
                    ErrorCode.CATEGORY_HAS_POSTS,
                    "카테고리 UUID: " + categoryRequest.getCategoryUuid()
                            + "는 게시글이 존재하여 삭제할 수 없습니다.",
                    "CategoryServiceImpl",
                    "deleteCategory");
        }

        log.info("[CategoryServiceImpl] deleteCategory 메서드 - 유효성 검사 통과 후 카테고리 삭제 진행");

        toBeDeletedAllCategories.get().add(categoryRequest.getCategoryUuid());

    }

    private void saveChildrenCategories(List<CategoryRequest> childrenRequests, Long userId) {

        log.info("[CategoryServiceImpl] saveChildrenCategories 메서드 시작");

        long childOrderIndex = 0;
        List<Category> childCategories = new ArrayList<>();
        for (CategoryRequest childRequest : childrenRequests) {

            // childCategories.add(saveCategoryHierarchy(childRequest,
            // childOrderIndex, userId));

            toBeSavedAllCategories.get().add(saveCategoryHierarchy(childRequest, childOrderIndex, userId));

            childOrderIndex++;

        }
        // categoryRepository.saveAll(childCategories);
    }

    // 부모 카테고리가 DB에 없는 경우 및 그 외 기타 상황 처리
    // 새롭게 요청으로 추가된 최상위 카테고리 저장 및 자식은 재귀적으로 저장.
    private Category createSingleCategory(CategoryRequest categoryRequest,
            long orderIndex, String parentCategoryId, Long userId) {

        log.info("[CategoryServiceImpl] createSingleCategory 메서드 시작");

        String parentCategoryUuid;

        if (parentCategoryId != null) {

            log.info(
                    "[CategoryServiceImpl] createSingleCategory 메서드: parentCategoryId가 null이 아닐 때 분기 진행");

            parentCategoryUuid = parentCategoryId;

        } else {

            log.info(
                    "[CategoryServiceImpl] createSingleCategory 메서드: parentCategoryId가 null일 때 분기 진행");

            parentCategoryUuid = null;
        }

        // .id(categoryRequest.getCategoryUuid()) // id값을 먼저 지정하면 (jpa에서 이미 DB에 존재하는
        // 엔티티라고 판단 isNew가 x). 따라서 save 전에 추가 조회 쿼리 발생 ㅠㅠ
        Category category = Category.builder()
                .userId(userId)
                .name(categoryRequest.getName())
                .parentId(parentCategoryUuid)
                .orderIndex(orderIndex)
                .build();
        // category.setId(categoryRequest.getCategoryUuid());

        // 자식 카테고리 생성 및 저장
        if (categoryRequest.getChildren() != null && !categoryRequest.getChildren().isEmpty()) {

            log.info(
                    "[CategoryServiceImpl] createSingleCategory 메서드 자식 카테고리가 존재하는 경우 분기 진행");

            long childOrderIndex = 0;

            // List<Category> childCategories = new ArrayList<>();

            for (CategoryRequest childRequest : categoryRequest.getChildren()) {
                Category childCategory = createSingleCategory(childRequest, childOrderIndex,
                        categoryRequest.getCategoryUuidParent(), userId);

                // childCategories.add(childCategory);

                toBeSavedAllCategories.get().add(childCategory);

                childOrderIndex++;

            }
            // categoryRepository.saveAll(childCategories);

        }

        return category;
    }

    private Category saveCategoryHierarchy(CategoryRequest categoryRequest,
            long orderIndex, Long userId) {
        log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 시작");

        // 부모까지 한번에 가져올 수 없는 이유는, 프론트측에서 자식까지 한번에 설정하기 때문에(즉 프론트에서 먼저 설정) 아직 DB에는 부모가
        // 설정되지 않은 상태이기 때문
        Optional<Category> existingCategory = categoryRepository.findById(categoryRequest.getCategoryUuid());
        Category category;

        // 초기 부모 카테고리가 이미 DB에 있는 경우에만 실행, 초기에 부모 카테고리가 DB에 없으면 관련 자식 까지
        // createSingleCategory에서 모두 처리
        if (existingCategory.isPresent()) {

            log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 DB에 카테고리가 존재하는 경우 분기 진행");

            category = existingCategory.get();
            category.setName(categoryRequest.getName());

            // orderIndex를 사용하여 순서 설정
            category.setOrderIndex(orderIndex);

            // 부모 설정
            String parentCategoryUuid;

            if (categoryRequest.getCategoryUuidParent() != null) {

                log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 부모 카테고리가 존재하는 경우 분기 진행");

                parentCategoryUuid = categoryRequest.getCategoryUuidParent();
            } else {

                log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 부모 카테고리가 존재하지 않는 경우 분기 진행");

                parentCategoryUuid = null;
            }

            category.setParentId(parentCategoryUuid);

            if (categoryRequest.getChildren() != null && !categoryRequest.getChildren().isEmpty()) {
                saveChildrenCategories(categoryRequest.getChildren(), userId);
            }

        } else {

            log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 DB에 카테고리가 존재하지 않는 경우 분기 진행");

            if (categoryRequest.getCategoryUuidParent() != null) {

                log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 DB에 카테고리가 존재하지 않는 경우 및 자식 카테고리인 경우 분기 진행");

                category = createSingleCategory(categoryRequest, orderIndex, categoryRequest.getCategoryUuidParent(),
                        userId);
            } else {

                log.info(
                        "[CategoryServiceImpl] saveCategoryHierarchy 메서드 DB에 카테고리가 존재하지 않는 경우 및 최상위(부모) 카테고리인 경우 분기 진행");

                // 새로운 부모 카테고리 생성
                category = createSingleCategory(categoryRequest, orderIndex, null, userId);
            }

        }

        return category;
    }

}
