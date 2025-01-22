package com.yhs.blog.springboot.jpa.domain.post.repository.search.document;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Log4j2
public class FeaturedImageDocument {

    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String fileName;

    @Field(type = FieldType.Keyword)
    private String fileUrl;

    @Field(type = FieldType.Keyword)
    private String fileType;

    @Field(type = FieldType.Keyword)
    private Long fileSize;

    public static FeaturedImageDocument from(FeaturedImage featuredImage) {

        return FeaturedImageDocument.builder().id(String.valueOf(featuredImage.getId()))
                .fileName(featuredImage.getFileName())
                .fileUrl(featuredImage.getFileUrl()).fileType(featuredImage.getFileType())
                .fileSize(featuredImage.getFileSize())
                .build();
    }
}
