package org.example.modules.user.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String nickname;
    /** admin | author | reader */
    private String role;
    /** 正常 | 禁用 | 待审核 */
    private String status;
    /** optional data URL */
    private String avatar;
}
