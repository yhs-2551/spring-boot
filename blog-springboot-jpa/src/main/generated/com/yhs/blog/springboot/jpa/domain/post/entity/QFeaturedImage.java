package com.yhs.blog.springboot.jpa.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFeaturedImage is a Querydsl query type for FeaturedImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeaturedImage extends EntityPathBase<FeaturedImage> {

    private static final long serialVersionUID = 121426547L;

    public static final QFeaturedImage featuredImage = new QFeaturedImage("featuredImage");

    public final com.yhs.blog.springboot.jpa.common.entity.QBaseEntity _super = new com.yhs.blog.springboot.jpa.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath fileName = createString("fileName");

    public final NumberPath<Long> fileSize = createNumber("fileSize", Long.class);

    public final StringPath fileType = createString("fileType");

    public final StringPath fileUrl = createString("fileUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public QFeaturedImage(String variable) {
        super(FeaturedImage.class, forVariable(variable));
    }

    public QFeaturedImage(Path<? extends FeaturedImage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFeaturedImage(PathMetadata metadata) {
        super(FeaturedImage.class, metadata);
    }

}

