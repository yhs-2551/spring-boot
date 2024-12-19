package com.yhs.blog.springboot.jpa.domain.post.event.elasticsearch;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;

public sealed interface PostEvent permits PostCreatedEvent, PostUpdatedEvent, PostDeletedEvent {
    Post getPost();
}
