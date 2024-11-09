//다대다(Many-to-Many) 관계를 **중간 엔티티(PostTag)**를 통해 설정

package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@IdClass(PostTagId.class)  // 복합 키 클래스를
@Table(name = "PostTags")
public class PostTag {

    // 여러개의 태그가 하나의 포스트에 쓰일 수 있음
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // 여러개의 포스트가 하나의 태그를 가질 수 있음
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public static PostTag create(Post post, Tag tag, User user) {
        PostTag postTag = new PostTag();
        postTag.setPost(post);
        postTag.setTag(tag);
        postTag.setUser(user);
        return postTag;

    }
}

