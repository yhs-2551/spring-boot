package com.yhs.blog.springboot.jpa.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddUserRequest {
    private String username;
    private String email;
    private String password;
}
