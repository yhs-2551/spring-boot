package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "Posts", indexes = {
        @Index(name = "idx_posts_user_id", columnList = "user_id"),
        @Index(name = "idx_posts_category_id", columnList = "category_id")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    private String content;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PostStatus publicStatus = PostStatus.PRIVATE;

    public enum PostStatus {

        PUBLIC, PRIVATE
    }

}
