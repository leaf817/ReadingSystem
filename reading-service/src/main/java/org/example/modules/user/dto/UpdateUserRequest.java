package org.example.modules.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String username;
    private String status;
    private String avatar;
}
