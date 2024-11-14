package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.CategoryRequest;
import com.yhs.blog.springboot.jpa.dto.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.dto.CategoryResponse;
import com.yhs.blog.springboot.jpa.entity.Category;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.exception.ResourceNotFoundException;
import com.yhs.blog.springboot.jpa.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.repository.UserRepository;
import com.yhs.blog.springboot.jpa.service.CategoryService;
import com.yhs.blog.springboot.jpa.service.UserService;
import com.yhs.blog.springboot.jpa.util.CategoryMapper;
import com.yhs.blog.springboot.jpa.util.TokenUtil;
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

        // In service
        for (Category category : savedCategories) {
            log.info("Category saved: id={}, name={}, parentId={}, childrenCount={}",
                    category.getId(),
                    category.getName(),
                    category.getParent() != null ? category.getParent().getId() : "null",
                    category.getChildren() != null ? category.getChildren().size() : 0
            );
        }
        // DTO로 변환. 변환 중에 Map을 이용해 캐시 사용
        Map<String, CategoryResponse> cache = new HashMap<>();
        return savedCategories.stream()
                .map(category -> CategoryMapper.toDTO(category, cache))
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
        return categories.stream().map(this::convertToResponseDTO).collect(Collectors.toList());


    }


    private CategoryResponse convertToResponseDTO(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                null,
                category.getChildren() != null && !category.getChildren().isEmpty() ?
                        category.getChildren().stream()
                                .map(subCategory -> new CategoryResponse(
                                        subCategory.getId(),
                                        subCategory.getName(),
                                        category.getId(),
                                        Collections.emptyList())) // 2단계 자식은 자식이 존재하지 않으니 빈배열로
                                .collect(Collectors.toList()) : Collections.emptyList() // 최상위 자식이 없으면 빈배열로 반환
                // 자식이 없으면 빈배열로 반환
        );
    }



    @Transactional
    private void deleteCategory(String categoryUuid) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryUuid);
        if (categoryOptional.isPresent()) {
            log.info("Deleting category with UUID: {}", categoryUuid);
            categoryRepository.delete(categoryOptional.get());
        } else {
            throw new ResourceNotFoundException("Category not found for UUID: " + categoryUuid);
        }
    }


    private User extractUserFromToken() {
        Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
        return userService.findUserById(userId);
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

        User user = extractUserFromToken();

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
