package com.yhs.blog.springboot.jpa.domain.category.controller;

import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser; 
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryWithChildrenResponse;
import com.yhs.blog.springboot.jpa.aop.performance.MeasurePerformance;
import com.yhs.blog.springboot.jpa.common.response.BaseResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "카테고리 등록, 카테고리 조회", description = "카테고리 생성, 수정, 삭제, 조회 API")
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/{blogId}/categories")
public class CategoryController {

        private final CategoryService categoryService;

        // CUD작업시 jpa 내부 구현 동작인 조회 후 업데이트(조회후 변경되지 않았으면 업데이트 추가 쿼리x), 새롭게 데이터 insert쿼리, 삭제시 jpa 원래 기능: 조회 후 삭제 -> 조회 없이 바로 삭제하도록 JPQL로 구현. 
        // 1000개 카테고리 삽입/수정/삭제 작업 시 500~600ms
        @Operation(summary = "카테고리 생성, 수정, 삭제 요청 처리", description = "사용자가 카테고리를 (생성, 수정, 삭제) 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "카테고리 작업 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "400", description = "해당 카테고리에 자식이 있거나 게시글이 존재하면 삭제 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "해당 사용자가 아니기에 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "카테고리 조회 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

        })
        @Parameter(name = "blogId", description = "사용자 블로그 아이디", required = true)
        @MeasurePerformance
        @PostMapping
        @PreAuthorize("#userBlogId == authentication.name")
        public ResponseEntity<BaseResponse> createCategory(@RequestBody CategoryRequestPayLoad categoryRequestPayLoad,
                        @P("userBlogId") @PathVariable("blogId") String blogId,
                        @AuthenticationPrincipal BlogUser blogUser) {

                log.info("[CategoryController] createCategory() 요청");

                categoryService.createCategory(categoryRequestPayLoad, blogUser);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new SuccessResponse<>(
                                                "카테고리 생성에 성공하였습니다."));
        }

        @MeasurePerformance
        // 특정 사용자의 모든 카테고리 조회. 모든 사용자가 볼 수 있어야 해서 preauthorize 제거
        // 상대적으로 자주 조회되며, 변경이 적은 데이터 redis사용 미세한 속도 개선 및 DB 부하 감소
        // Cateogry를 가져올때 자식 및 연관된 게시글을 한번에 가져옴으로써 네트워크
        // 요청감소(한 번에 가져오면 단점은 메모리양이 증가하긴 하지만), 성능 향상. -> 관계 매핑 제거 변경 후, 카테고리 1000개 기준 Redis로 캐시 평균 속도 10ms이내, Redis캐시 미사용시 25ms. 조회 시 한번의 쿼리안에 서브쿼리까지 포함, 필요한 컬럼(필드)만 조회해서 최적화, 조회시 필요한 인덱스 사용. 데이터 양이 많지 않아 인덱스를 위한 공간이 부담스럽지 않다고 판단  
        @Operation(summary = "카테고리 조회 요청 처리", description = "사용자가 카테고리 조회 요청을 보내면 해당 요청을 처리")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "카테고리 조회 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),

        })
        @Parameter(name = "blogId", description = "사용자 블로그 아이디", required = true)
        @GetMapping
        public ResponseEntity<BaseResponse> getAllCategoriesWithChildrenByUserId(
                        @PathVariable("blogId") String blogId, HttpServletRequest request) {

                log.info("[CategoryController] getAllCategoriesWithChildrenByUserId() 요청");

                List<CategoryWithChildrenResponse> categories = categoryService
                                .getAllCategoriesWithChildrenByUserId(blogId);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(new SuccessResponse<List<CategoryWithChildrenResponse>>(categories,
                                                "카테고리 조회에 성공하였습니다."));
        }

}
