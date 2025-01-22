package com.yhs.blog.springboot.jpa.domain.post.dto.request;

import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;
import lombok.*;

import java.util.List;


@NoArgsConstructor
@Getter
@ToString
// 유효성 검사 프론트랑 맞추기 위해 추가 및 수정 필요
public class PostRequest {
    // private Long id;             // 게시글 ID

    // private Long userId;          // 작성자 ID

    // private String username;        // 작성자명

    private String categoryName;      // 카테고리 이름
 
    private String title;         // 게시글 제목

    private String content;       // 게시글 내용

    private List<String> tags;   // 태그

    private List<FileRequest> files; // 첨부파일

    private List<String> deleteTempImageUrls; // 최종 발행 시 클라우드 저장소에 저장되어 있는 사용되지 않는 이미지 및 파일 삭제하는 URL

//    @NotNull(message = "공개 상태를 선택하세요.")
//    @Pattern(regexp = "PUBLIC|PRIVATE", message = "'PUBLIC' 또는 'PRIVATE' 둘 중 하나의 상태로 선택하세요.")
    private String postStatus;    // 게시글 상태 (PUBLIC, PRIVATE)

    private String commentsEnabled; // 댓글 허용 여부

    private FeaturedImageRequest featuredImage; // 대표 이미지

    // private int views;            // 조회수

    // private int commentCount;     // 댓글 수

    // private int replyCount;       // 대댓글 수

    @Builder
    public PostRequest(String categoryName, String title,
                       String content, List<String> tags, List<FileRequest> files,
                       List<String> deleteTempImageUrls, String postStatus,
                       String commentsEnabled, FeaturedImageRequest featuredImageRequest
                       ) {
        this.categoryName = categoryName;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.files = files;
        this.deleteTempImageUrls = deleteTempImageUrls;
        this.postStatus = postStatus;
        this.commentsEnabled = commentsEnabled;
        this.featuredImage = featuredImageRequest;

    }

}
