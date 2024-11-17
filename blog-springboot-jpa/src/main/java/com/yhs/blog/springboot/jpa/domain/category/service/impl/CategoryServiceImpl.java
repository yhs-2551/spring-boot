package com.yhs.blog.springboot.jpa.domain.category.service.impl;

import com.yhs.blog.springboot.jpa.security.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequest;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.exception.custom.ResourceNotFoundException;
import com.yhs.blog.springboot.jpa.domain.category.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.domain.category.mapper.CategoryMapper;
import com.yhs.blog.springboot.jpa.security.jwt.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final HttpServletRequest request;

    @Override
    @Transactional
    public List<CategoryResponse> createCategory(CategoryRequestPayLoad categoryRequestPayLoad) {

        log.info("categoryRequestPayLoad >>>>>>> " + categoryRequestPayLoad);

        // 카테고리 삭제 작업
        if (categoryRequestPayLoad.getCategoryToDelete() != null && !categoryRequestPayLoad.getCategoryToDelete().isEmpty()) {
            categoryRequestPayLoad.getCategoryToDelete().forEach(this::deleteCategory);
        }

        // 모든 엔티티 먼저 일괄 저장
        List<Category> savedCategories = new ArrayList<>();
        long orderIndex = 0L; // 프론트측에서 요청이 오는 순서대로 orderIndex 값을 설정한다.
        // 최종적으로 프론트에서 설정한 카테고리 구조를 그대로 화면에 보여주기 위함.
        for (CategoryRequest categoryRequest : categoryRequestPayLoad.getCategories()) {
            savedCategories.add(saveCategoryHierarchy(categoryRequest, orderIndex));
            orderIndex++;

        }

        // DTO로 변환. 변환 중에 Map을 이용해 캐시 사용
        Map<String, CategoryResponse> cache = new HashMap<>();
        return savedCategories.stream()
                .map(category -> CategoryMapper.from(category, cache))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesWithChildrenByUserId() {

        Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);

        List<Category> categories = categoryRepository.findAllWithChildrenByUserId(userId);

        if (categories.isEmpty()) {
            log.info("No categories found for user ID: {}", userId);
            return Collections.emptyList();  // 불변 빈 배열 반환
        }
        return categories.stream().map(CategoryMapper::of).collect(Collectors.toList());


    }

    @Transactional
    private void deleteCategory(String categoryUuid) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryUuid);
        if (categoryOptional.isPresent()) {

            Category category = categoryOptional.get();

            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                throw new IllegalStateException("Category with UUID: " + categoryUuid + " cannot be deleted because it has child categories.");
            }
            if (category.getPosts() != null && !category.getPosts().isEmpty()) {
                throw new IllegalStateException("Category with UUID: " + categoryUuid + " cannot be deleted because it has posts.");
            }
            log.info("Deleting category with UUID: {}", categoryUuid);
            categoryRepository.delete(category);

        } else {
            throw new ResourceNotFoundException("Category not found for UUID: " + categoryUuid);
        }
    }


    private List<Category> saveChildrenCategories(List<CategoryRequest> childrenRequests) {
        long childOrderIndex = 0;
        List<Category> childCategories = new ArrayList<>();
        for (CategoryRequest childRequest : childrenRequests) {
            childCategories.add(saveCategoryHierarchy(childRequest,
                    childOrderIndex));
            childOrderIndex++;
        }
        return childCategories;
    }

    // 새롭게 요청으로 추가된 최상위 카테고리 저장 및 자식은 재귀적으로 저장.
    private Category createSingleCategory(CategoryRequest categoryRequest,
                                          long orderIndex) {

        Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
        User user = userService.findUserById(userId);

        Category parentCategory;

        if (categoryRequest.getCategoryUuidParent() != null) {
            parentCategory = categoryRepository.findById(categoryRequest.getCategoryUuidParent())
                    .orElse(null);
        } else {
            parentCategory = null;
        }

        Category category = Category.builder()
                .id(categoryRequest.getCategoryUuid())
                .user(user)
                .name(categoryRequest.getName())
                .parent(parentCategory) // 초기에 부모 설정은 null
                .orderIndex(orderIndex)
                .children(Collections.emptyList()) // 초기에는 빈 리스트로 설정
                .build();

        // 자식 카테고리 생성 및 저장
        if (categoryRequest.getChildren() != null && !categoryRequest.getChildren().isEmpty()) {
            long childOrderIndex = 0;
            List<Category> childCategories = new ArrayList<>();
            for (CategoryRequest childRequest : categoryRequest.getChildren()) {
                Category childCategory = createSingleCategory(childRequest, childOrderIndex);
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

        // Check if the category exists
        Optional<Category> existingCategory = categoryRepository.findById(categoryRequest.getCategoryUuid());
        Category category;

        if (existingCategory.isPresent()) {
            category = existingCategory.get();
            category.setName(categoryRequest.getName());


            // Set new parent category if it exists
            Category parentCategory = categoryRequest.getCategoryUuidParent() != null
                    ? Category.builder().id(categoryRequest.getCategoryUuidParent()).build()
                    : null;

            // Set new children categories with incremented orderIndex
            List<Category> newChildren = categoryRequest.getChildren() != null && !categoryRequest.getChildren().isEmpty()
                    ? saveChildrenCategories(categoryRequest.getChildren())
                    : Collections.emptyList();


            category.setParent(parentCategory);
            category.setChildren(newChildren);
            category.setOrderIndex(orderIndex);

        } else {
            // 새로운 부모 및 자식 카테고리 생성
            category = createSingleCategory(categoryRequest, orderIndex);

        }


        // 새롭게 생성 시 또는 수정 시 한번의 save 메서드로 처리 CASCADE.PERSIST를 이용하여 부모가 저장될때 자식도 같이 저장되게 처리
        return categoryRepository.save(category);
    }

}
