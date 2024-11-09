package com.yhs.blog.springboot.jpa.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
//복합키를 정의하기 위한 ID 클래스
public class PostTagId implements Serializable {
    private Long post;
    private Long tag;
    private Long user;
}
