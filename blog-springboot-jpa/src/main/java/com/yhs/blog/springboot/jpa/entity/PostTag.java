package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@IdClass(PostTagId.class)  // 복합 키 클래스를 명시
@Table(name = "PostTags", indexes = {
        @Index(name = "idx_post_tags_post_id", columnList = "post_id"),
        @Index(name = "idx_post_tags_tag_id", columnList = "tag_id")
})
public class PostTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}

