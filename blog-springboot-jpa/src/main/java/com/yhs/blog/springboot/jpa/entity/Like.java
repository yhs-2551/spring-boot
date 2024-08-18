package com.yhs.blog.springboot.jpa.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Likes", indexes = {
        @Index(name = "idx_likes_post_id", columnList = "post_id"),
        @Index(name = "idx_likes_user_id", columnList = "user_id"),

}, uniqueConstraints = {
                @UniqueConstraint(columnNames = {"post_id", "user_id"})
        }
)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_Id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


}


