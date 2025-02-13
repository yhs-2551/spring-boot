package com.yhs.blog.springboot.jpa.domain.post.entity;

import com.yhs.blog.springboot.jpa.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Tag extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    public Tag(String name) {
        this.name = name;
    }

}
