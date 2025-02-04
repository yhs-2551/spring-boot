package com.yhs.blog.springboot.jpa.domain.file.entity;

import com.yhs.blog.springboot.jpa.common.entity.BaseEntity;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_files_post_id", columnList = "post_id") // post_id에 인덱스 추가
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class File extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = true)
    private Integer width;

    @Column(nullable = true)
    private Integer height;

    @Column(nullable = false)
    private Long postId;

    @Builder
    public File(String fileName, String fileType, String fileUrl, Long fileSize, Integer width, Integer height,
            Long postId) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.postId = postId;
    }

}
