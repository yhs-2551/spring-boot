package com.yhs.blog.springboot.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostTagId implements Serializable {
    private Long post;
    private Long tag;
}
