package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.CategoryDocument;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.PostDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@NoArgsConstructor
public class PostResponse {

        private Long id; // 게시글 ID

        // private Long userId; // 작성자 ID

        private String username; // 작성자명

        // private String blogId; // 블로그 ID

        private String categoryName; // 카테고리 이름

        private String title; // 게시글 제목

        private String content; // 게시글 내용

        // private List<String> tags; // 태그

        // private List<FileResponse> files;

        // private String postStatus; // 게시글 상태 (PUBLIC, PRIVATE)

        // private String commentsEnabled; // 댓글 허용 여부

        private FeaturedImageResponse featuredImage; // 대표 이미지

        private String featuredImageUrl; // 수정 필요 위에 featuredImage 날리고 이것만 남기는거 고려

        private LocalDateTime createdAt; // 생성 일시

        // private LocalDateTime updatedAt; // 수정 일시

        // private int views; // 조회수

        // private int commentCount; // 댓글 수

        // private int replyCount; // 대댓글 수

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
                // response.userId = post.getUser().getId();
                response.username = post.getUser().getUsername();
                // response.blogId = post.getUser().getBlogId();
                response.categoryName = Optional.ofNullable(post.getCategory())
                        .map(Category::getName)
                        .orElse(null);
                response.title = post.getTitle();
                response.content = post.getContent().replace("/temp/", "/final/");

                // response.tags = Optional.ofNullable(post.getPostTags())
                // .map(postTags -> postTags.stream()
                // .map(postTag -> postTag.getTag().getName())
                // .collect(Collectors.toList()))
                // .orElse(null);

                // response.files = Optional.ofNullable(post.getFiles())
                // .map(files -> files.stream()
                // .map(FileResponse::new)
                // .collect(Collectors.toList()))
                // .orElse(null);
                //
                // response.postStatus = post.getPostStatus().name();
                // response.commentsEnabled = post.getCommentsEnabled().name();
                response.featuredImage = Optional.ofNullable(post.getFeaturedImage())
                        .map(FeaturedImageResponse::from)
                        .orElse(null);

                response.createdAt = post.getCreatedAt();
                // response.updatedAt = post.getUpdatedAt();
                // response.views = post.getViews();
                // response.commentCount = post.getCommentCount();
                // response.replyCount = post.getReplyCount();

                return response;
        }

        public static PostResponse fromDocument(PostDocument document) {
                PostResponse response = new PostResponse();
                response.id = Long.parseLong(document.getId());
                response.featuredImage = Optional.ofNullable(document.getFeaturedImage())
                        .map(FeaturedImageResponse::from).orElse(null);
                response.title = document.getTitle();
                response.content = document.getContent();
                response.categoryName = Optional.ofNullable(document.getCategory())
                        .map(CategoryDocument::getName)
                        .orElse(null);
                response.createdAt = document.getCreatedAt();
                response.username = document.getUsername();
                // response.blogId = document.getBlogId();
                return response;
        }
}
