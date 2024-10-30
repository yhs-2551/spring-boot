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
    // 태그 엔티티도 cascade = CascadeType.ALL, orphanRemoval = true 필수. 없다면 delete()를 할때 PostTag엔티티에서
    // 외래키를 소유하고 있기 때문에 제약조건에 위배되어 삭제할 수 없음.
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
