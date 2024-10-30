package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

//나중에 첨부파일 컬럼도 추가해야 한다.

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
public class Post extends BaseEntity{
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


    @Setter
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<File> files;


    // 하나의 게시글이 여러 태그를 가질 수 있음
    // 태그의 경우 글 작성시와 글 조회시 순서가 보장되어야 하기 때문에 List로 정의.
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags;


    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private PostStatus postStatus;

    public enum PostStatus {
        PUBLIC, PRIVATE
    }

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private CommentsEnabled commentsEnabled;

    public enum CommentsEnabled {
        ALLOW, DISALLOW
    }

    @OneToOne
    @JoinColumn(name = "featured_image_id", nullable = true) // 대표 이미지가 있을 수도, 없을 수도 있음
    private FeaturedImage featuredImage;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "views", nullable = true)
    private int views = 0;  // 조회수 기본값은 0

    @Column(name = "comment_count", nullable = true)
    private int commentCount = 0;  // 총 댓글 수 기본값은 0

    @Column(name = "reply_count", nullable = true)
    private int replyCount = 0;  // 대댓글 수 기본값은 0



    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;


    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes;

    @Builder
    public Post(User user, Category category, String title, String content,
                PostStatus postStatus, CommentsEnabled commentsEnabled, FeaturedImage featuredImage) {
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
                       FeaturedImage featuredImage
                       ) {
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

