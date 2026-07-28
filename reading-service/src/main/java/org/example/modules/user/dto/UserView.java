package org.example.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserView {
    private Long id;
    private String username;
    private String nickname;
    /** admin | author | reader */
    private String role;
    /** 正常 | 禁用 | 待审核 */
    private String status;
    private String joinedAt;
    private String avatar;
    private String bio;
}
