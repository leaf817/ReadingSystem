package org.example.modules.user.service;

import org.example.common.exception.BusinessException;
import org.example.common.response.ResponseCode;
import org.example.modules.user.dto.CreateUserRequest;
import org.example.modules.user.dto.UpdateUserRequest;
import org.example.modules.user.dto.UserView;
import org.example.modules.user.entity.UserAccount;
import org.example.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
public class UserAdminService {

    private static final Set<String> VALID_STATUS = Set.of("正常", "禁用", "待审核");
    private static final Set<String> VALID_ROLE = Set.of("admin", "author", "reader");

    private final UserRepository userRepository;

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserView> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public UserView createUser(CreateUserRequest request) {
        String nickname = safeTrim(request.getNickname());
        String username = safeTrim(request.getUsername());
        String password = request.getPassword() == null ? "" : request.getPassword();
        String role = safeTrim(request.getRole()).toLowerCase();
        String status = safeTrim(request.getStatus());
        if (status.isEmpty()) {
            status = "正常";
        }

        if (nickname.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入昵称");
        }
        if (nickname.length() < 2) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "昵称至少 2 个字符");
        }
        if (username.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入账号");
        }
        if (username.length() < 3) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "账号至少 3 个字符");
        }
        if (password.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入密码");
        }
        if (password.length() < 6) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "密码至少 6 位");
        }
        if (!VALID_ROLE.contains(role)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "角色不合法");
        }
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "状态不合法");
        }
        if ("admin".equals(role)) {
            status = "正常";
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "该账号已存在");
        }

        String joinedAt = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        UserAccount account = new UserAccount(
                null, username, password, nickname, role, status, joinedAt, blankToNull(request.getAvatar()), null);
        userRepository.save(account);
        return toView(account);
    }

    @Transactional
    public UserView updateUser(Long id, UpdateUserRequest request) {
        UserAccount user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.BAD_REQUEST, "用户不存在"));

        String nickname = safeTrim(request.getNickname());
        String username = safeTrim(request.getUsername());
        String status = safeTrim(request.getStatus());

        if (nickname.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入昵称");
        }
        if (nickname.length() < 2) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "昵称至少 2 个字符");
        }
        if (username.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入账号");
        }
        if (username.length() < 3) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "账号至少 3 个字符");
        }
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "状态不合法");
        }
        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "该账号已存在");
        }

        if ("admin".equals(user.getRole())) {
            if (!"admin".equals(username)) {
                throw new BusinessException(ResponseCode.BAD_REQUEST, "管理员账号不可修改");
            }
            status = "正常";
        }

        user.setNickname(nickname);
        user.setUsername(username);
        user.setStatus(status);
        user.setAvatar(blankToNull(request.getAvatar()));
        userRepository.save(user);
        return toView(user);
    }

    private UserView toView(UserAccount user) {
        return ProfileService.toView(user);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
