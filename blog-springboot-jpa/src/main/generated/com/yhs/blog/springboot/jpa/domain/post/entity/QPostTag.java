package com.yhs.blog.springboot.jpa.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPostTag is a Querydsl query type for PostTag
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostTag extends EntityPathBase<PostTag> {

    private static final long serialVersionUID = 2143154176L;

    public static final QPostTag postTag = new QPostTag("postTag");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> postId = createNumber("postId", Long.class);

    public final NumberPath<Long> tagId = createNumber("tagId", Long.class);

    public QPostTag(String variable) {
        super(PostTag.class, forVariable(variable));
    }

    public QPostTag(Path<? extends PostTag> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPostTag(PathMetadata metadata) {
        super(PostTag.class, metadata);
    }

}

