package com.cuutruyen.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Integer userId;
    private String username;
    private String role;
}
