package com.yhs.blog.springboot.jpa.domain.category.controller;


import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.exception.custom.ResourceNotFoundException;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/{blogId}/categories")
public class CategoryApiController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("#userBlogId == authentication.name")
    public ResponseEntity<ApiResponse> createCategory(@RequestBody CategoryRequestPayLoad categoryRequestPayLoad,
                                                      @P("userBlogId") @PathVariable("blogId") String blogId) {

        List<CategoryResponse> categoryResponse =
                categoryService.createCategory(categoryRequestPayLoad);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<List<CategoryResponse>>(categoryResponse, "Success " +
                        "create new category."));
    }

    // 특정 사용자의 모든 카테고리 조회. 모든 사용자가 볼 수 있어야 해서 preauthorize 제거
    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategoriesWithChildrenByUserId(@PathVariable("blogId") String blogId) {

        User user = userRepository.findByBlogId(blogId)
                .orElseThrow(() -> new ResourceNotFoundException(blogId + "사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        List<CategoryResponse> categories = categoryService.getAllCategoriesWithChildrenByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<List<CategoryResponse>>(categories, "Success " +
                        "get all categories."));
    }


}
