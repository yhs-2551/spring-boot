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
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

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

    // 이전 참조 해제하고, 새로운 참조를 등록하기 위해 사용. A collection with cascade="all-delete-orphan" was no
    // longer referenced by the owning entity instance 오류 방지를 위함. 외부에서 getChildren으로 가져온 후 clear를
    // 하게되면 getChildren()을 통해 불필요하게 데이터 요청이 생기게 되어 이를 방지하기 위함.
    // children은 fetch가 LAZY이기 때문에 그 순간 여러 자식들이 불필요하게 호출된다면 성능상 이슈가 발생할 수 있다.
//    public void update(Category parentCategory, List<Category> newChildren) {
//        // 이전 참조를 clear
//        this.parent = null;
//        this.children.clear();
//
//        // 새로운 참조를 설정
//        this.parent = parentCategory;
//        this.children.addAll(newChildren);
//    }
}
