package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "Files", indexes = {
        @Index(name = "idx_files_post_id", columnList = "post_id")  // post_id에 인덱스 추가
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class File extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;
    @Column(nullable = false)
    private String filetType;
    @Column(nullable = false)
    private String fileUrl;
    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = true)
    private Integer width;

    @Column(nullable = true)
    private Integer height;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

}
