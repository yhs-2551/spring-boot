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

    private String categoryName; // 카테고리 이름

    private String title; // 게시글 제목

    private String content; // 게시글 내용

    private List<String> tags; // 태그

    private List<String> editPageDeletedTags;

    private List<FileRequest> files; // 첨부파일

    private List<String> deleteTempImageUrls; // 최종 발행 시 클라우드 저장소에 저장되어 있는 사용되지 않는 이미지 및 파일 삭제하는 URL

    private String postStatus; // 게시글 상태 (PUBLIC, PRIVATE)

    private String commentsEnabled; // 댓글 허용 여부

    private FeaturedImageRequest featuredImage; // 대표 이미지

}
