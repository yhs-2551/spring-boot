package com.yhs.blog.springboot.jpa.domain.post.dto.request;

import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;
import lombok.*;

import java.util.List;

// json -> 객체로 역직렬화시 @setter가 없다면 Reflection을 사용하여 값을 주입
@NoArgsConstructor
@Getter
@ToString
@Setter // 테스트 작성시 필요해서 setter 사용
// 유효성 검사 프론트랑 맞추기 위해 추가 및 수정 필요
public class PostRequest {

    private String categoryName; // 카테고리 이름

    private String title; // 게시글 제목

    private String content; // 게시글 내용

    private List<String> tags; // 태그

    private List<FileRequest> files; // 첨부파일

    private List<String> deletedImageUrlsInFuture; // 최종 발행 시 클라우드 저장소에 저장되어 있는 사용되지 않는 이미지 및 파일 삭제하는 URL

    private String postStatus; // 게시글 상태 (PUBLIC, PRIVATE)

    private String commentsEnabled; // 댓글 허용 여부 (ALLOW, DISALLOW)

    private FeaturedImageRequest featuredImage; // 대표 이미지

    // private int views; // 조회수

    // private int commentCount; // 댓글 수

    // private int replyCount; // 대댓글 수

}
