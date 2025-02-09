package com.yhs.blog.springboot.jpa.domain.post.entity;

import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 연관관계 매핑을 사용하지 않아 직접 alter table을 통해 외래키 제약조건 설정해야하고,
@ToString
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_created_at_id", columnList = "created_at DESC, id DESC"), // explain에서 사용되진 않았는데, 혹시 다른데서 사용될 수 있으니 보류 
        @Index(name = "idx_posts_user_id", columnList = "user_id"),
        @Index(name = "idx_posts_category_id", columnList = "category_id"), // 카테고리조회시 필요해서 필수.
        @Index(name = "idx_posts_featured_image_id", columnList = "featured_image_id"),
})
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private PostStatus postStatus = PostStatus.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private CommentsEnabled commentsEnabled = CommentsEnabled.ALLOW;

    @Column(name = "featured_image_id", nullable = true) // 대표 이미지가 있을 수도, 없을 수도 있음 
    private Long featuredImageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = true)
    private String categoryId;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Post(Long userId, String categoryId, String title, String content,
            PostStatus postStatus, CommentsEnabled commentsEnabled, Long featuredImageId) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.postStatus = postStatus;
        this.commentsEnabled = commentsEnabled;
        this.featuredImageId = featuredImageId;
    }

    public void update(String categoryId, Long featuredImageId, String title, String content,
            PostStatus postStatus,
            CommentsEnabled commentsEnabled) {
        this.categoryId = categoryId;
        this.featuredImageId = featuredImageId;
        this.title = title;
        this.content = content;
        this.postStatus = postStatus;
        this.commentsEnabled = commentsEnabled;
    }

}
