package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.file.dto.response.FileResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

// 파일 및 대표이미지의 경우 전체가 아닌 url값만 넘겨주는거 고려헀으나, 프론트에서 전체 정보를 다시 담아서 재요청해야할수도 있기 때문에 전체 필드 필요 
@Getter
@NoArgsConstructor
public class PostResponse {

        private Long id; // 게시글 식별자. 수정 페이지 및 상세페이지에는 필요x(프론트측 params에서 뽑아 쓰기 때문)

        private String username; // 작성자명

        @Nullable
        private String categoryName; // 카테고리 이름

        private String title; // 게시글 제목

        private String content; // 게시글 내용

        private String postStatus; // 게시글 상태

        @Nullable
        private List<String> tags; // 태그

        @Nullable
        private List<FileResponse> files;

        @Nullable
        private FeaturedImageResponse featuredImage; // 대표 이미지

        private LocalDateTime createdAt; // 생성 일시, LocalDateTime은 Spring Boot에서 자동으로 ISO-8601 형식으로 변환. Redis같은거만 설정해주면 됨

        // queryDSL index page 응답 + 특정 사용자 페이지의 소유주가 아니면 postStatus가 필요 없음. 공개 비공개를 보여줄
        // 필요가 없기 때문
        public PostResponse(Long id, String title, String content, String username,
                        String categoryName, FeaturedImageResponse featuredImage) {
                this.id = id;
                this.title = title;
                this.content = content;
                this.username = username;
                this.categoryName = categoryName;
                this.featuredImage = featuredImage; // null이면 자동 null로 처리
        }

        // queryDSL user page 응답. 나중에 해당 블로그의 소유주만 postStatus응답을 받음으로써 목록페이지에서 공개/비공개
        // 보여줌. 블로그의 당사자만 postStatus가 필요하고, 당사자가 아닌 경우는 필요 없는데 이 부분 나중에 처리 필요.
        public PostResponse(Long id, String title, String content, PostStatus postStatus, String username,
                        String categoryName,
                        FeaturedImageResponse featuredImage) {
                this.id = id;
                this.title = title;
                this.content = content;
                this.postStatus = postStatus.name();
                this.username = username;
                this.categoryName = categoryName;
                this.featuredImage = featuredImage;
        }

        // 게시글 상세 페이지 관련 응답
        public PostResponse(String title, String content, List<String> tags,
                        List<FileResponse> files,
                        PostStatus postStatus, String username, String categoryName, LocalDateTime createdAt) {
                this.title = title;
                this.content = content;
                this.tags = tags;
                this.files = files;
                this.postStatus = postStatus.name();
                this.username = username;
                this.categoryName = categoryName;
                this.createdAt = createdAt;
        }

        // 게시글 수정 페이지에서 해당 게시글에 관한 정보 응답 
        public PostResponse(String title, String content, List<String> tags,
                        List<FileResponse> files,
                        FeaturedImageResponse featuredImage,
                        PostStatus postStatus, String username, String categoryName, LocalDateTime createdAt) {
                this.title = title;
                this.content = content;
                this.tags = tags;
                this.files = files;
                this.featuredImage = featuredImage;
                this.postStatus = postStatus.name();
                this.username = username;
                this.categoryName = categoryName;
                this.createdAt = createdAt;
        }

        // public static PostResponse fromForDetail(Post post) {
        //         PostResponse response = new PostResponse();

        //         response.title = post.getTitle();
        //         response.content = post.getContent().replace("/temp/", "/final/");

        //         response.tags = Optional.ofNullable(post.getPostTags())
        //                         .map(postTags -> postTags.stream()
        //                                         .map(postTag -> postTag.getTag().getName())
        //                                         .collect(Collectors.toList()))
        //                         .orElse(null);

        //         response.files = Optional.ofNullable(post.getFiles())
        //                         .map(files -> files.stream()
        //                                         .map(FileResponse::new)
        //                                         .collect(Collectors.toList()))
        //                         .orElse(null);

        //         response.postStatus = post.getPostStatus().name();

        //         response.username = post.getUser().getUsername();
        //         response.categoryName = Optional.ofNullable(post.getCategory())
        //                         .map(Category::getName)
        //                         .orElse(null);

        //         response.createdAt = post.getCreatedAt();

        //         return response;
        // }

        // public static PostResponse fromForEdit(Post post) {
        //         PostResponse response = new PostResponse();

        //         response.tags = Optional.ofNullable(post.getPostTags())
        //                         .map(postTags -> postTags.stream()
        //                                         .map(postTag -> postTag.getTag().getName())
        //                                         .collect(Collectors.toList()))
        //                         .orElse(null);

        //         response.files = Optional.ofNullable(post.getFiles())
        //                         .map(files -> files.stream()
        //                                         .map(FileResponse::new)
        //                                         .collect(Collectors.toList()))
        //                         .orElse(null);

        //         response.postStatus = post.getPostStatus().name();
        //         response.username = post.getUser().getUsername();
        //         response.categoryName = Optional.ofNullable(post.getCategory())
        //                         .map(Category::getName)
        //                         .orElse(null);
        //         response.title = post.getTitle();
        //         response.content = post.getContent().replace("/temp/", "/final/");
        //         response.featuredImage = Optional.ofNullable(post.getFeaturedImage()) // 프론트에서 수정페이지에 접근할때 대표 이미지의 정보가
        //                                                                               // 필요함
        //                         .map(FeaturedImageResponse::from)
        //                         .orElse(null);

        //         response.createdAt = post.getCreatedAt();

        //         return response;
        // }

}
