//다대다(Many-to-Many) 관계를 **중간 엔티티(PostTag)**를 통해 설정

package com.yhs.blog.springboot.jpa.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;

// @IdClass(PostTagId.class) // 복합 키 관련 클래스는 엔티티간 연간관계 매핑 + 복합키 사용이 있을때만 사용하면 됨(+@IdClass 어노테이션 사용)
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "post_tags", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_tag", columnNames = { "post_id", "tag_id" })
}, indexes = {
        @Index(name = "idx_post_tags_tag_id", columnList = "tag_id") // post_Id만 필요한 인덱스는 위에 복합 인덱스의 첫 번째 컬럼이므로 위 복합 인덱스에서 post_id 단독으로 사용 가능
})
public class PostTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    public PostTag(Long postId, Long tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }
}
