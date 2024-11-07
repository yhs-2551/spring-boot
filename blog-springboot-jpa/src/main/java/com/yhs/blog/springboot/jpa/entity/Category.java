package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "Categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "user_id"})
})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Category extends BaseEntity {

    @Id
    @Column(nullable = false, length = 36, unique = true)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 여러 사용자가 동일한 카테고리명을 가질 순 있지만, 한 사용자가 동일한 카테고리를 가질 수 없게 위에서 unique 제약조건을 걸음.
    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true) // 여러 자식이 동일한 부모 id값을 가질 수 있기 때문에 unique = true 제약조건을 걸지 않음.
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Category> children;

//    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Post> posts;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "order_index", nullable = false)
    private Long orderIndex;

}
