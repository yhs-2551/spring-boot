package com.yhs.blog.springboot.jpa.domain.category.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCategory is a Querydsl query type for Category
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategory extends EntityPathBase<Category> {

    private static final long serialVersionUID = -634999178L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCategory category = new QCategory("category");

    public final com.yhs.blog.springboot.jpa.common.entity.QBaseEntity _super = new com.yhs.blog.springboot.jpa.common.entity.QBaseEntity(this);

    public final ListPath<Category, QCategory> children = this.<Category, QCategory>createList("children", Category.class, QCategory.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath id = createString("id");

    public final StringPath name = createString("name");

    public final NumberPath<Long> orderIndex = createNumber("orderIndex", Long.class);

    public final QCategory parent;

    public final SetPath<com.yhs.blog.springboot.jpa.domain.post.entity.Post, com.yhs.blog.springboot.jpa.domain.post.entity.QPost> posts = this.<com.yhs.blog.springboot.jpa.domain.post.entity.Post, com.yhs.blog.springboot.jpa.domain.post.entity.QPost>createSet("posts", com.yhs.blog.springboot.jpa.domain.post.entity.Post.class, com.yhs.blog.springboot.jpa.domain.post.entity.QPost.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.yhs.blog.springboot.jpa.domain.user.entity.QUser user;

    public QCategory(String variable) {
        this(Category.class, forVariable(variable), INITS);
    }

    public QCategory(Path<? extends Category> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCategory(PathMetadata metadata, PathInits inits) {
        this(Category.class, metadata, inits);
    }

    public QCategory(Class<? extends Category> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parent = inits.isInitialized("parent") ? new QCategory(forProperty("parent"), inits.get("parent")) : null;
        this.user = inits.isInitialized("user") ? new com.yhs.blog.springboot.jpa.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

