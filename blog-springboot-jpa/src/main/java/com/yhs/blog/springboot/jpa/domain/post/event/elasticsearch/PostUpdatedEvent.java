package com.yhs.blog.springboot.jpa.domain.post.event.elasticsearch;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;

public record PostUpdatedEvent(Post post) implements PostEvent {
    @Override
    public Post getPost() {
        return post;
    }
}
