package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

//나중에 첨부파일 컬럼도 추가해야 한다.

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@NoArgsConstructor
@Table(name = "Posts", indexes = {
        @Index(name = "idx_posts_user_id", columnList = "user_id"),
        @Index(name = "idx_posts_category_id", columnList = "category_id")
})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    User 임시로 null 값 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "views", nullable = true)
    private int views = 0;  // 조회수 기본값은 0

    @Column(name = "comment_count", nullable = true)
    private int commentCount = 0;  // 총 댓글 수 기본값은 0

    @Column(name = "reply_count", nullable = true)
    private int replyCount = 0;  // 대댓글 수 기본값은 0


    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private PostStatus postStatus;


    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostTag> postTags;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes;


    public enum PostStatus {

        PUBLIC, PRIVATE
    }

    @Builder
    public Post(User user, String title, String content, Category category, PostStatus postStatus) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.category = category;
        this.postStatus = postStatus;
    }

    public void update(String title, String content, PostStatus postStatus, Category category) {
        this.title = title;
        this.content = content;
        this.postStatus = postStatus;
        this.category = category;
    }


}

