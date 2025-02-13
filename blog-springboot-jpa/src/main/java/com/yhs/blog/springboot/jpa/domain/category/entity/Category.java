package com.yhs.blog.springboot.jpa.domain.category.entity;

import com.yhs.blog.springboot.jpa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories", uniqueConstraints = {
        // unique제약조건으로 인해 인덱스 자동 생성됨
        @UniqueConstraint(columnNames = { "name", "user_id" }),
}, indexes = {
        // where절에서 orderBy와 같이 사용
        @Index(name = "idx_categories_user_id_parent_id_order_index", columnList = "user_id, parent_id, order_index"),
        // 자식의 join시 parentId와 orderIndex를 같이 사용
        @Index(name = "idx_categories_parent_id_order_index", columnList = "parent_id, order_index"),
})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
// 부모 카테고리 - 자식 카테고리 단방향 매핑: 객체 그래프 탐색을 자주 사용하며(연관 엔티티인 자식 엔티티가 자주 조회됨. 즉 카테고리
// 페이지에 접근하면 카테고리 자식이 필요 ) + 카테고리의 자식의 데이터가 많지 않음. 이러한 이유로 인해 단방향 매핑 사용
public class Category extends BaseEntity {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @PrePersist
    public void generateId() {
        if (id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }

    // 여러 사용자가 동일한 카테고리명을 가질 순 있지만, 한 사용자가 동일한 카테고리를 가질 수 없게 위에서 unique 제약조건을 걸음.
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "order_index", nullable = false)
    private Long orderIndex;

    // 여러 자식이 동일한 부모 id값을 가질 수 있기 때문에 unique = true 제약조건을 걸지 않음.
    // JPA에서 별로도 참조하는 컬럼을 명시하지 않으면 자동으로 해당 테이블의 id 컬럼 참조. 즉 여기에서 id 컬럼 참조
    @Column(name = "parent_id", nullable = true)
    private String parentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
