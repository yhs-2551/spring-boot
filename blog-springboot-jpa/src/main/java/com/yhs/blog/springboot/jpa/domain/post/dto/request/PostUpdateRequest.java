package com.yhs.blog.springboot.jpa.domain.post.dto.request;

import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@ToString
public class PostUpdateRequest {

    private String categoryName;      // 카테고리 이름

    private String title;         // 게시글 제목

    private String content;       // 게시글 내용

    private List<String> tags;   // 태그

    private List<String> editPageDeletedTags;

    private List<FileRequest> files; // 첨부파일

    private List<String> deleteTempImageUrls; // 최종 발행 시 클라우드 저장소에 저장되어 있는 사용되지 않는 이미지 및 파일 삭제하는 URL

    //    @NotNull(message = "공개 상태를 선택하세요.")
//    @Pattern(regexp = "PUBLIC|PRIVATE", message = "'PUBLIC' 또는 'PRIVATE' 둘 중 하나의 상태로 선택하세요.")
    private String postStatus;    // 게시글 상태 (PUBLIC, PRIVATE)

    private String commentsEnabled; // 댓글 허용 여부

    private FeaturedImageRequest featuredImage; // 대표 이미지

//    private int views;            // 조회수
//
//    private int commentCount;     // 댓글 수
//
//    private int replyCount;       // 대댓글 수

}
