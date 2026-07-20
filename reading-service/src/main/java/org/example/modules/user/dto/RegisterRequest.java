package org.example.modules.user.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String nickname;
}
