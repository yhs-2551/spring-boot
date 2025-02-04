package com.yhs.blog.springboot.jpa.domain.post.entity;

import com.yhs.blog.springboot.jpa.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(name = "featured_images")
public class FeaturedImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;   // 파일명

    @Column(nullable = false)
    private String fileUrl;    // 파일 URL

    @Column(nullable = false)
    private String fileType;   // 파일 타입

    @Column(nullable = false)
    private Long fileSize;     // 파일 사이즈 (바이트 단위)

    @Builder
    public FeaturedImage(String fileName, String fileUrl, String fileType, Long fileSize) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }
}
