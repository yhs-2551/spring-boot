package com.yhs.blog.springboot.jpa.domain.post.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPost is a Querydsl query type for Post
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPost extends EntityPathBase<Post> {

    private static final long serialVersionUID = -1518758598L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPost post = new QPost("post");

    public final com.yhs.blog.springboot.jpa.common.entity.QBaseEntity _super = new com.yhs.blog.springboot.jpa.common.entity.QBaseEntity(this);

    public final com.yhs.blog.springboot.jpa.domain.category.entity.QCategory category;

    public final NumberPath<Integer> commentCount = createNumber("commentCount", Integer.class);

    public final SetPath<Comment, QComment> comments = this.<Comment, QComment>createSet("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final EnumPath<com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled> commentsEnabled = createEnum("commentsEnabled", com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QFeaturedImage featuredImage;

    public final SetPath<com.yhs.blog.springboot.jpa.domain.file.entity.File, com.yhs.blog.springboot.jpa.domain.file.entity.QFile> files = this.<com.yhs.blog.springboot.jpa.domain.file.entity.File, com.yhs.blog.springboot.jpa.domain.file.entity.QFile>createSet("files", com.yhs.blog.springboot.jpa.domain.file.entity.File.class, com.yhs.blog.springboot.jpa.domain.file.entity.QFile.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final SetPath<Like, QLike> likes = this.<Like, QLike>createSet("likes", Like.class, QLike.class, PathInits.DIRECT2);

    public final EnumPath<com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus> postStatus = createEnum("postStatus", com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus.class);

    public final ListPath<PostTag, QPostTag> postTags = this.<PostTag, QPostTag>createList("postTags", PostTag.class, QPostTag.class, PathInits.DIRECT2);

    public final NumberPath<Integer> replyCount = createNumber("replyCount", Integer.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.yhs.blog.springboot.jpa.domain.user.entity.QUser user;

    public final NumberPath<Integer> views = createNumber("views", Integer.class);

    public QPost(String variable) {
        this(Post.class, forVariable(variable), INITS);
    }

    public QPost(Path<? extends Post> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPost(PathMetadata metadata, PathInits inits) {
        this(Post.class, metadata, inits);
    }

    public QPost(Class<? extends Post> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.yhs.blog.springboot.jpa.domain.category.entity.QCategory(forProperty("category"), inits.get("category")) : null;
        this.featuredImage = inits.isInitialized("featuredImage") ? new QFeaturedImage(forProperty("featuredImage")) : null;
        this.user = inits.isInitialized("user") ? new com.yhs.blog.springboot.jpa.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

