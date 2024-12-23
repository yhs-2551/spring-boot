package com.yhs.blog.springboot.jpa.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1033622288L;

    public static final QUser user = new QUser("user");

    public final com.yhs.blog.springboot.jpa.common.entity.QBaseEntity _super = new com.yhs.blog.springboot.jpa.common.entity.QBaseEntity(this);

    public final StringPath blogId = createString("blogId");

    public final SetPath<com.yhs.blog.springboot.jpa.domain.category.entity.Category, com.yhs.blog.springboot.jpa.domain.category.entity.QCategory> categories = this.<com.yhs.blog.springboot.jpa.domain.category.entity.Category, com.yhs.blog.springboot.jpa.domain.category.entity.QCategory>createSet("categories", com.yhs.blog.springboot.jpa.domain.category.entity.Category.class, com.yhs.blog.springboot.jpa.domain.category.entity.QCategory.class, PathInits.DIRECT2);

    public final SetPath<com.yhs.blog.springboot.jpa.domain.post.entity.Comment, com.yhs.blog.springboot.jpa.domain.post.entity.QComment> comments = this.<com.yhs.blog.springboot.jpa.domain.post.entity.Comment, com.yhs.blog.springboot.jpa.domain.post.entity.QComment>createSet("comments", com.yhs.blog.springboot.jpa.domain.post.entity.Comment.class, com.yhs.blog.springboot.jpa.domain.post.entity.QComment.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final SetPath<com.yhs.blog.springboot.jpa.domain.file.entity.File, com.yhs.blog.springboot.jpa.domain.file.entity.QFile> files = this.<com.yhs.blog.springboot.jpa.domain.file.entity.File, com.yhs.blog.springboot.jpa.domain.file.entity.QFile>createSet("files", com.yhs.blog.springboot.jpa.domain.file.entity.File.class, com.yhs.blog.springboot.jpa.domain.file.entity.QFile.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final SetPath<com.yhs.blog.springboot.jpa.domain.post.entity.Like, com.yhs.blog.springboot.jpa.domain.post.entity.QLike> likes = this.<com.yhs.blog.springboot.jpa.domain.post.entity.Like, com.yhs.blog.springboot.jpa.domain.post.entity.QLike>createSet("likes", com.yhs.blog.springboot.jpa.domain.post.entity.Like.class, com.yhs.blog.springboot.jpa.domain.post.entity.QLike.class, PathInits.DIRECT2);

    public final StringPath password = createString("password");

    public final SetPath<com.yhs.blog.springboot.jpa.domain.post.entity.Post, com.yhs.blog.springboot.jpa.domain.post.entity.QPost> posts = this.<com.yhs.blog.springboot.jpa.domain.post.entity.Post, com.yhs.blog.springboot.jpa.domain.post.entity.QPost>createSet("posts", com.yhs.blog.springboot.jpa.domain.post.entity.Post.class, com.yhs.blog.springboot.jpa.domain.post.entity.QPost.class, PathInits.DIRECT2);

    public final SetPath<com.yhs.blog.springboot.jpa.domain.post.entity.PostTag, com.yhs.blog.springboot.jpa.domain.post.entity.QPostTag> postTags = this.<com.yhs.blog.springboot.jpa.domain.post.entity.PostTag, com.yhs.blog.springboot.jpa.domain.post.entity.QPostTag>createSet("postTags", com.yhs.blog.springboot.jpa.domain.post.entity.PostTag.class, com.yhs.blog.springboot.jpa.domain.post.entity.QPostTag.class, PathInits.DIRECT2);

    public final EnumPath<User.UserRole> role = createEnum("role", User.UserRole.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath username = createString("username");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

