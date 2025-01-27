package com.yhs.blog.springboot.jpa.domain.category.service.impl;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.cache.CacheConstants;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequest;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.domain.category.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserFindService;
import com.yhs.blog.springboot.jpa.domain.category.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserFindService userFindService;
    private final RedisTemplate<String, List<CategoryResponse>> categoryResponseRedisTemplate;
    private User user;
    boolean isParentCategoryExist = true;

    @Override
    @Transactional
    public void createCategory(CategoryRequestPayLoad categoryRequestPayLoad, String blogId) {

        log.info("[CategoryServiceImpl] createCategory 메서드 시작");

        // 카테고리 삭제 작업
        if (categoryRequestPayLoad.getCategoryToDelete() != null
                && !categoryRequestPayLoad.getCategoryToDelete().isEmpty()) {

            log.info("[CategoryServiceImpl] createCategory 메서드 - 삭제할 카테고리가 있을 경우 삭제할 카테고리 분기 진행");

            categoryRequestPayLoad.getCategoryToDelete().forEach(this::deleteCategory);
            // 삭제된 엔티티들 즉 영속성 컨텍스트 변경 사항을 즉시 DB에 반영. 이게 없으면, 프론트에서 부모에서 자식 카테고리를 없애고 해당 부모
            // 카테고리를 제거할 때 duplicate key 에러 발생
            // 만약 동일한 트랜잭션 내에서 이후에 작업이 실패하면 트랜잭션 완료가 아니기 때문에 flush로 작업한 내용도 롤백됨. 트랜잭션의 원자성
            // 보장
            categoryRepository.flush();
        }

        user = userFindService.findUserByBlogId(blogId);

        List<Category> categories = new ArrayList<>();

        long orderIndex = 0L; // 프론트측에서 요청이 오는 순서대로 orderIndex 값을 설정한다.
        // 최종적으로 프론트에서 설정한 카테고리 구조를 그대로 화면에 보여주기 위함.
        for (CategoryRequest categoryRequest : categoryRequestPayLoad.getCategories()) {
            categories.add(saveCategoryHierarchy(categoryRequest, orderIndex));
            orderIndex++;
        }

        categoryRepository.saveAll(categories);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        // 카테고리 작업 시 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화
                        categoryResponseRedisTemplate.delete("categories:" + blogId);
                    }

                });

    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesWithChildrenByUserId(String blogId) {

        log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 시작");

        String key = "categories:" + blogId;

        List<CategoryResponse> redisCategories = categoryResponseRedisTemplate.opsForValue().get(key);

        if (redisCategories != null && !redisCategories.isEmpty()) {

            log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 Redis 캐시에 존재하는 경우 분기 시작");

            return redisCategories;
        }

        log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 Redis 캐시에 존재하지 않는 경우 분기 시작");

        Long userId = userFindService.findUserByBlogId(blogId).getId();

        List<Category> dbCategories = categoryRepository.findAllWithChildrenAndPostsByUserId(userId);

        if (dbCategories.isEmpty()) {

            log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 DB에 카테고리가 존재하지 않는 경우 분기 진행");

            return Collections.emptyList(); // 불변 빈 배열 반환
        }

        log.info("[CategoryServiceImpl] getAllCategoriesWithChildrenByUserId 메서드 DB에 카테고리가 존재하는 경우 분기 진행");

        List<CategoryResponse> categories = dbCategories.stream().map(CategoryMapper::from)
                .collect(Collectors.toList());

        categoryResponseRedisTemplate.opsForValue().set(key, categories, CacheConstants.CATEGORY_CACHE_HOURS,
                TimeUnit.HOURS);

        return categories;

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

    @Loggable
    private void deleteCategory(CategoryRequest categoryRequest) {

        log.info("[CategoryServiceImpl] deleteCategory 메서드 시작");

        Optional<Category> categoryOptional = categoryRepository
                .findByIdWithChildrenAndPosts(categoryRequest.getCategoryUuid());

        if (categoryOptional.isPresent()) {

            log.info("[CategoryServiceImpl] deleteCategory 메서드 - 삭제할 카테고리가 존재하는 경우 분기 진행");

            Category category = categoryOptional.get();

            if (category.getChildren() != null && !category.getChildren().isEmpty()) {

                throw new BusinessException(
                        ErrorCode.CATEGORY_HAS_CHILDREN,
                        "카테고리 UUID: " + category.getId() + "는 자식 카테고리가 존재하여 삭제할 수 없습니다.",
                        "CategoryServiceImpl",
                        "deleteCategory");

            }

            if (category.getPosts() != null && !category.getPosts().isEmpty()) {

                throw new BusinessException(
                        ErrorCode.CATEGORY_HAS_POSTS,
                        "카테고리 UUID: " + category.getId()
                                + "는 게시글이 존재하여 삭제할 수 없습니다.",
                        "CategoryServiceImpl",
                        "deleteCategory");
            }

            log.info("[CategoryServiceImpl] deleteCategory 메서드 - 유효성 검사 통과 후 카테고리 삭제 진행");

            categoryRepository.delete(category);

        } else {

            return; // 프론트에서 카테고리 새롭게 생성하고 바로 삭제할 경우 해당 카테고리가 DB에 존재하지 않아도 삭제할 수 있도록 처리

            // throw new BusinessException(
            // ErrorCode.CATEGORY_NOT_FOUND,
            // "카테고리 UUID: " + categoryRequest.getCategoryUuid() + "를 찾을 수 없습니다.",
            // "CategoryServiceImpl",
            // "deleteCategory");
        }
    }

    private List<Category> saveChildrenCategories(List<CategoryRequest> childrenRequests) {

        log.info("[CategoryServiceImpl] saveChildrenCategories 메서드 시작");

        long childOrderIndex = 0;
        List<Category> childCategories = new ArrayList<>();
        for (CategoryRequest childRequest : childrenRequests) {
            childCategories.add(saveCategoryHierarchy(childRequest,
                    childOrderIndex));
            childOrderIndex++;
        }
        return childCategories;
    }

    // 부모 카테고리가 DB에 없는 경우 및 그 외 기타 상황 처리
    // 새롭게 요청으로 추가된 최상위 카테고리 저장 및 자식은 재귀적으로 저장.
    private Category createSingleCategory(CategoryRequest categoryRequest,
            long orderIndex, boolean isParentCategoryAlreadyExistInDB) {

        log.info("[CategoryServiceImpl] createSingleCategory 메서드 시작");

        Category parentCategory;

        if (isParentCategoryAlreadyExistInDB) {

            log.info(
                    "[CategoryServiceImpl] createSingleCategory 메서드 DB에 부모 카테고리가 이미 존재하는 경우 및 자식 카테고리가 DB에 존재하지 않는 경우 분기 진행");

            // 새로운 카테고리를 생성하고 이미 존재하는 부모에게 자식으로 추가할 때 처리 로직
            parentCategory = categoryRepository.findById(categoryRequest.getCategoryUuidParent())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND,
                            categoryRequest.getCategoryUuidParent() + " 카테고리를 찾을 수 없습니다.", "CategoryServiceImpl",
                            "createSingleCategory"));
        } else {

            log.info(
                    "[CategoryServiceImpl] createSingleCategory 메서드 DB에 부모 카테고리가 존재하지 않는 경우 분기 진행");

            parentCategory = null;
        }

        Category category = Category.builder()
                .id(categoryRequest.getCategoryUuid())
                .user(user)
                .name(categoryRequest.getName())
                .parent(parentCategory)
                .orderIndex(orderIndex)
                .children(Collections.emptyList()) // 초기에는 빈 리스트로 설정
                .build();

        // 자식 카테고리 생성 및 저장
        if (categoryRequest.getChildren() != null && !categoryRequest.getChildren().isEmpty()) {

            log.info(
                    "[CategoryServiceImpl] createSingleCategory 메서드 자식 카테고리가 존재하는 경우 분기 진행");

            long childOrderIndex = 0;
            List<Category> childCategories = new ArrayList<>();
            for (CategoryRequest childRequest : categoryRequest.getChildren()) {
                Category childCategory = createSingleCategory(childRequest, childOrderIndex, false);
                childCategory.setParent(category); // 자식에서 부모 설정
                childCategories.add(childCategory);
                childOrderIndex++;
            }

            category.setChildren(childCategories);

        }

        return category;// 최종적으로 부모 카테고리 리턴
    }

    private Category saveCategoryHierarchy(CategoryRequest categoryRequest,
            long orderIndex) {
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

            Category parentCategory;

            if (categoryRequest.getCategoryUuidParent() != null) {

                log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 부모 카테고리가 존재하는 경우 분기 진행");

                // id값만 build해서 설정하는 것보다 양방향 매핑이라 부모 엔티티 자체를 넘겨주는게 안전
                parentCategory = categoryRepository
                        .findById(categoryRequest.getCategoryUuidParent()).get();
            } else {

                log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 부모 카테고리가 존재하지 않는 경우 분기 진행");

                parentCategory = null;
            }

            // orderIndex 순서를 사용하여 children 설정
            List<Category> newChildren = categoryRequest.getChildren() != null
                    && !categoryRequest.getChildren().isEmpty()
                            ? saveChildrenCategories(categoryRequest.getChildren())
                            : Collections.emptyList();

            category.setParent(parentCategory);
            category.setChildren(newChildren);
            category.setOrderIndex(orderIndex);

        } else {

            log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 DB에 카테고리가 존재하지 않는 경우 분기 진행");

            if (categoryRequest.getCategoryUuidParent() != null) {

                log.info("[CategoryServiceImpl] saveCategoryHierarchy 메서드 DB에 카테고리가 존재하지 않는 경우 및 자식 카테고리인 경우 분기 진행");

                category = createSingleCategory(categoryRequest, orderIndex, true);
            } else {

                log.info(
                        "[CategoryServiceImpl] saveCategoryHierarchy 메서드 DB에 카테고리가 존재하지 않는 경우 및 최상위(부모) 카테고리인 경우 분기 진행");

                // 새로운 부모 카테고리 생성
                category = createSingleCategory(categoryRequest, orderIndex, false);
            }

        }

        // 새롭게 생성 시 또는 수정 시 한번의 save 메서드로 처리 CASCADE.PERSIST를 이용하여 부모가 저장될때 자식도 같이 저장되게
        // 처리
        return category;
    }

}
