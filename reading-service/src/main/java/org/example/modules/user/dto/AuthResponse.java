package org.example.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String nickname;
    private String displayName;
    private String role;
    private String token;
    private String avatar;
}
