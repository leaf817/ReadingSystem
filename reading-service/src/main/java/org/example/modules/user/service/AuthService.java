package org.example.modules.user.service;

import org.example.common.exception.BusinessException;
import org.example.common.response.ResponseCode;
import org.example.modules.user.dto.AuthResponse;
import org.example.modules.user.dto.LoginRequest;
import org.example.modules.user.dto.RegisterRequest;
import org.example.modules.user.entity.UserAccount;
import org.example.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest request) {
        String username = safeTrim(request.getUsername());
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (username.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入账号");
        }
        if (password.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入密码");
        }

        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResponseCode.BAD_REQUEST, "账号不存在，请先注册"));

        if (!user.getPassword().equals(password)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "密码错误");
        }
        if ("禁用".equals(user.getStatus())) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "账号已被禁用");
        }

        return toAuthResponse(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = safeTrim(request.getUsername());
        String nickname = safeTrim(request.getNickname());
        String password = request.getPassword() == null ? "" : request.getPassword();
        String confirm = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

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
        if (!password.equals(confirm)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "两次密码不一致");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "账号已存在，请直接登录");
        }

        String joinedAt = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        UserAccount account = new UserAccount(null, username, password, nickname, "reader", "正常", joinedAt, null);
        userRepository.save(account);
        return toAuthResponse(account);
    }

    private AuthResponse toAuthResponse(UserAccount user) {
        return new AuthResponse(
                user.getUsername(),
                user.getNickname(),
                user.getNickname(),
                user.getRole(),
                UUID.randomUUID().toString().replace("-", ""),
                user.getAvatar()
        );
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
