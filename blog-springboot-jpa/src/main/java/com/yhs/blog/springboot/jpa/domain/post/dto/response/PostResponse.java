package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post; 
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@NoArgsConstructor
public class PostResponse {

        private Long id; // 게시글 ID

        private String username; // 작성자명

        private String categoryName; // 카테고리 이름

        private String title; // 게시글 제목

        private String content; // 게시글 내용

        private FeaturedImageResponse featuredImage; // 대표 이미지

        private String featuredImageUrl; // 수정 필요 위에 featuredImage 날리고 이것만 남기는거 고려

        private LocalDateTime createdAt; // 생성 일시

        // querydsl 응답에 필요. N+1문제 해결 및 필요한 데이터만 응답
        public PostResponse(Long id, String title, String content, String username, String categoryName,
                        FeaturedImage featuredImage) {
                this.id = id;
                this.title = title;
                this.content = content;
                this.username = username;
                this.categoryName = categoryName;
                this.featuredImage = Optional.ofNullable(featuredImage).map(FeaturedImageResponse::from)
                                .orElse(null);
        }

        public static PostResponse from(Post post) {
                PostResponse response = new PostResponse();

                response.id = post.getId();
                response.username = post.getUser().getUsername();
                response.categoryName = Optional.ofNullable(post.getCategory())
                                .map(Category::getName)
                                .orElse(null);
                response.title = post.getTitle();
                response.content = post.getContent().replace("/temp/", "/final/");
                response.featuredImage = Optional.ofNullable(post.getFeaturedImage())
                                .map(FeaturedImageResponse::from)
                                .orElse(null);

                response.createdAt = post.getCreatedAt();

                return response;
        } 
}
