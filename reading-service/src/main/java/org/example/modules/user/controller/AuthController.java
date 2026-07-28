package org.example.modules.user.controller;

import org.example.common.response.ResponseResult;
import org.example.modules.user.dto.AuthResponse;
import org.example.modules.user.dto.ChangePasswordRequest;
import org.example.modules.user.dto.LoginRequest;
import org.example.modules.user.dto.RegisterRequest;
import org.example.modules.user.dto.UpdateProfileRequest;
import org.example.modules.user.dto.UserView;
import org.example.modules.user.service.AuthService;
import org.example.modules.user.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ProfileService profileService;

    public AuthController(AuthService authService, ProfileService profileService) {
        this.authService = authService;
        this.profileService = profileService;
    }

    @PostMapping("/login")
    public ResponseResult<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseResult.success(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseResult<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseResult.success(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseResult<UserView> me(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseResult.success(profileService.getMe(authorization));
    }

    @PutMapping("/me")
    public ResponseResult<UserView> updateMe(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody UpdateProfileRequest request) {
        return ResponseResult.success(profileService.updateMe(authorization, request));
    }

    @PutMapping("/password")
    public ResponseResult<Void> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(authorization, request);
        return ResponseResult.success();
    }
}
