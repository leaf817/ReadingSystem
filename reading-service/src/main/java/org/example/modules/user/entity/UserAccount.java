package org.example.modules.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 128)
    private String password;

    @Column(nullable = false, length = 64)
    private String nickname;

    /** admin | author | reader */
    @Column(nullable = false, length = 32)
    private String role;

    /** 正常 | 禁用 | 待审核 */
    @Column(nullable = false, length = 16)
    private String status;

    /** yyyy-MM-dd */
    @Column(name = "joined_at", nullable = false, length = 16)
    private String joinedAt;

    /** optional data URL */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String avatar;
}
