package org.example.modules.user.service;

import org.example.common.exception.BusinessException;
import org.example.common.response.ResponseCode;
import org.example.modules.user.dto.ChangePasswordRequest;
import org.example.modules.user.dto.UpdateProfileRequest;
import org.example.modules.user.dto.UserView;
import org.example.modules.user.entity.UserAccount;
import org.example.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final TokenStore tokenStore;

    public ProfileService(UserRepository userRepository, TokenStore tokenStore) {
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
    }

    public UserView getMe(String authorization) {
        UserAccount user = requireUser(authorization);
        return toView(user);
    }

    @Transactional
    public UserView updateMe(String authorization, UpdateProfileRequest request) {
        UserAccount user = requireUser(authorization);

        String nickname = safeTrim(request.getNickname());
        if (nickname.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入昵称");
        }
        if (nickname.length() < 2) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "昵称至少 2 个字符");
        }
        if (nickname.length() > 64) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "昵称过长");
        }

        String bio = request.getBio() == null ? "" : request.getBio().trim();
        if (bio.length() > 100) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "签名不能超过 100 字");
        }

        user.setNickname(nickname);
        user.setAvatar(blankToNull(request.getAvatar()));
        if ("author".equals(user.getRole()) || "reader".equals(user.getRole())) {
            user.setBio(blankToNull(bio));
        }
        userRepository.save(user);
        return toView(user);
    }

    @Transactional
    public void changePassword(String authorization, ChangePasswordRequest request) {
        UserAccount user = requireUser(authorization);

        String oldPassword = request.getOldPassword() == null ? "" : request.getOldPassword();
        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword();
        String confirm = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

        if (oldPassword.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入当前密码");
        }
        if (!user.getPassword().equals(oldPassword)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "当前密码不正确");
        }
        if (newPassword.isEmpty()) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "请输入新密码");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "新密码至少 6 位");
        }
        if (!newPassword.equals(confirm)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "两次新密码不一致");
        }
        if (newPassword.equals(oldPassword)) {
            throw new BusinessException(ResponseCode.BAD_REQUEST, "新密码不能与当前密码相同");
        }

        user.setPassword(newPassword);
        userRepository.save(user);
    }

    public static UserView toView(UserAccount user) {
        return new UserView(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getJoinedAt(),
                user.getAvatar(),
                user.getBio()
        );
    }

    private UserAccount requireUser(String authorization) {
        String token = extractBearer(authorization);
        String username = tokenStore.findUsername(token)
                .orElseThrow(() -> new BusinessException(ResponseCode.UNAUTHORIZED, "请先登录"));
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResponseCode.UNAUTHORIZED, "登录已失效，请重新登录"));
    }

    private String extractBearer(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "请先登录");
        }
        String value = authorization.trim();
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            value = value.substring(7).trim();
        }
        if (value.isEmpty()) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "请先登录");
        }
        return value;
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
