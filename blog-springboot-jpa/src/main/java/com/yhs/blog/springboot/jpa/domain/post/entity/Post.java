package com.yhs.blog.springboot.jpa.domain.post.entity;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.file.entity.File;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus; 
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

//나중에 첨부파일 컬럼도 추가해야 한다.
@ToString
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Posts", indexes = {
        @Index(name = "idx_posts_user_id", columnList = "user_id"),
        @Index(name = "idx_posts_category_id", columnList = "category_id"),
        @Index(name = "idx_posts_featured_image_id", columnList = "featured_image_id")
})
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<File> files;

    // 하나의 게시글이 여러 태그를 가질 수 있음
    // 태그의 경우 글 작성시와 글 조회시 순서가 보장되어야 하기 때문에 List로 정의.
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private PostStatus postStatus = PostStatus.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private CommentsEnabled commentsEnabled = CommentsEnabled.ALLOW;

    @OneToOne
    @JoinColumn(name = "featured_image_id", nullable = true) // 대표 이미지가 있을 수도, 없을 수도 있음
    private FeaturedImage featuredImage;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Builder
    public Post(Long id, User user, Category category, String title, String content,
            PostStatus postStatus, CommentsEnabled commentsEnabled, FeaturedImage featuredImage) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.postStatus = postStatus;
        this.commentsEnabled = commentsEnabled;
        this.featuredImage = featuredImage;
    }

    public void update(Category category, String title, String content, Set<File> newFiles,
            List<PostTag> newPostTags,
            PostStatus postStatus,
            CommentsEnabled commentsEnabled,
            FeaturedImage featuredImage) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.files.clear();
        this.files.addAll(newFiles);
        this.postTags.clear();
        this.postTags.addAll(newPostTags);
        this.postStatus = postStatus;
        this.commentsEnabled = commentsEnabled;
        this.featuredImage = featuredImage;
    }

}
