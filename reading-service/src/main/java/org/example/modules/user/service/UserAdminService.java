package org.example.modules.user.service;

import org.example.common.exception.BusinessException;
import org.example.common.response.ResponseCode;
import org.example.modules.user.dto.UpdateUserRequest;
import org.example.modules.user.dto.UserView;
import org.example.modules.user.entity.UserAccount;
import org.example.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class UserAdminService {

    private static final Set<String> VALID_STATUS = Set.of("正常", "禁用", "待审核");

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
        return new UserView(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getJoinedAt(),
                user.getAvatar()
        );
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
