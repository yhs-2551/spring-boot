package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    // 하나의 태그가 여러개의 포스트에 사용될 수 있음
    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostTag> postTags;

    @Builder
    public Tag(String name) {
        this.name = name;
    }

    public static Tag create(String name) {
        return Tag.builder().name(name).build();
    }
}
